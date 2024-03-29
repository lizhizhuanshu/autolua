package top.lizhistudio.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.protobuf.ByteString;
import com.immomo.mls.MLSEngine;
import com.immomo.mls.global.LVConfigBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.NotReleaseLuaFunctionAdapter;
import top.lizhistudio.app.core.ProjectManager;
import top.lizhistudio.app.core.ProjectManagerImplement;
import top.lizhistudio.app.core.Transport;
import top.lizhistudio.app.core.UserInterfaceImplement;
import top.lizhistudio.app.util.AssetManager;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.core.RemoteLuaContextManager;
import top.lizhistudio.autolua.core.value.LuaValue;
import top.lizhistudio.autolua.debugger.proto.DebugMessage;

public class DebugService extends Service {
    public static final String STATE_ACTION = DebugService.class.getName();
    public static final String ASK_STATE_ACTION = STATE_ACTION+".WhatState";
    private static final int NOTIFICATION_ID = 10086;
    private static final String CHANNEL_ONE_ID = "AutoLua";
    private boolean isStarted = false;
    private ServerSocket serverSocket;
    private volatile Transport transport;
    private StateAskReceiver stateAskReceiver;
    private String rootPath = null;

    public DebugService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onStartNotification();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ASK_STATE_ACTION);
        stateAskReceiver = new StateAskReceiver();
        localBroadcastManager.registerReceiver(stateAskReceiver,intentFilter);
    }


    private class StateAskReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            update(true);
        }
    }

    protected void onStartNotification()
    {
        String CHANNEL_ONE_NAME = "常驻服务设置";
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(CHANNEL_ONE_ID)
                .setContentText("调试器服务正在运行")
                .setSmallIcon(R.mipmap.auxiliary)
                .setWhen(System.currentTimeMillis());

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ONE_ID);

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(NOTIFICATION_ID,notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isStarted)
        {
            isStarted = true;
            int port = intent.getIntExtra("port",-1);
            int code = intent.getIntExtra("code", Activity.RESULT_CANCELED);
            Intent mediaIntent = intent.getParcelableExtra("data");
            if (code == Activity.RESULT_OK)
            {
                MediaProjectionManager mediaProjectionManager =
                        (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                mediaProjection = mediaProjectionManager.getMediaProjection(code,mediaIntent);
            }else
            {
                Toast.makeText(this,getString(R.string.cancelMediaProjectHint),Toast.LENGTH_LONG).show();
            }

            if (port != -1)
            {
                update(true);
                onStart(port);
            }else
            {
                Toast.makeText(this,"端口不能为空，启动调试器失败",Toast.LENGTH_SHORT).show();;
                stopSelf();
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(stateAskReceiver);
        Thread thread = workThread;
        if (thread != null)
        {
            thread.interrupt();
            workThread = null;
        }
        Transport transport = this.transport;
        if (transport != null)
        {
            transport.close();
            this.transport = null;
        }
        if (serverSocket != null)
        {
            try{
                serverSocket.close();
            }catch (IOException e)
            {
            }
            serverSocket = null;
        }
        update(false);
    }

    private volatile Thread workThread;

    private void onStart(int port)
    {
        workThread = new Thread()
        {
            @Override
            public void run() {
                try{
                    DebugService.this.run(port);
                }catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        };
        workThread.start();
    }

    private LocalBroadcastManager localBroadcastManager;

    private void update(boolean state)
    {
        Intent intent = new Intent(STATE_ACTION);
        intent.putExtra("state",state);
        localBroadcastManager.sendBroadcast(intent);
    }


    private String relativePath(String rootPath,String scriptPath)
    {
        if (scriptPath.charAt(0) == '@')
        {
            return scriptPath.substring(rootPath.length()+2);
        }
        return scriptPath;
    }

    private void sendLog(String source,int line,String message)
    {
        send(DebugMessage.Message.newBuilder()
                .setMethod(DebugMessage.METHOD.LOG)
                .setPath(relativePath(rootPath,source))
                .setLine(line)
                .setMessage(message).build());
    }


    private void send(DebugMessage.Message message)
    {
        try{
            transport.send(message);
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendStopped()
    {
        send(DebugMessage.Message.newBuilder().setMethod(DebugMessage.METHOD.STOPPED).build());
    }


    private void onExecuteFile(AutoLuaEngine interpreter, String projectName,String path)
    {
        ProjectManager projectManager = ProjectManagerImplement.getInstance();
        if (interpreter != null && !interpreter.isRunning())
        {
            String projectPath = projectManager.getProjectPath(projectName);
            if (projectPath!= null)
            {
                MLSEngine.setLVConfig(new LVConfigBuilder(DebugService.this)
                        .setRootDir(projectPath)
                        .setImageDir(projectPath+"/image")
                        .setCacheDir(DebugService.this .getCacheDir().getAbsolutePath())
                        .setGlobalResourceDir(projectPath+"/resource").build());
                //此处需要注意
                rootPath = projectPath;
                interpreter.executeFile(projectPath + "/" + path,
                        LuaContext.CODE_TYPE.TEXT_BINARY,new LuaInterpreter.Callback() {
                            @Override
                            public void onCallback(LuaValue[] result) {
                                sendStopped();
                            }
                            @Override
                            public void onError(Throwable throwable) {
                                if (throwable != null)
                                {
                                    throwable.printStackTrace();
                                    sendLog("unknown",0,throwable.getMessage());
                                }
                                sendStopped();
                            }
                        });
            }
        }
    }

    private void onGetInfo(String projectName)
    {
        ProjectManager projectManager = ProjectManagerImplement.getInstance();
        ProjectManager.ProjectInfo projectInfo = projectManager.getInfo(projectName);
        DebugMessage.Message.Builder builder = DebugMessage.Message.newBuilder();
        builder.setMethod(DebugMessage.METHOD.GET_INFO);
        if (projectInfo != null)
        {
            builder.setName(projectName)
                    .setVersion(projectInfo.version)
                    .setFeature(projectInfo.feature);
        }
        send(builder.build());
    }


    private void onHandler(AutoLuaEngine autoLuaEngine, DebugMessage.Message message)
    {
        ProjectManager projectManager = ProjectManagerImplement.getInstance();
        switch (message.getMethod())
        {
            case UNKNOWN:
                break;
            case INTERRUPT:
                autoLuaEngine.interrupt();
                break;
            case EXECUTE_FILE:
                onExecuteFile(autoLuaEngine, message.getName(),message.getPath());
                break;
            case GET_INFO:
                onGetInfo(message.getName());
                break;
            case DELETE_FILE:
                projectManager.deleteFile(message.getName(),message.getPath());
                break;
            case UPDATE_FILE:
                projectManager.updateFile(message.getName(),
                        message.getPath(),
                        message.getData().toByteArray());
                break;
            case CREATE_PROJECT:
                projectManager.createProject(message.getName(),message.getFeature(),message.getVersion());
                break;
            case DELETE_PROJECT:
                projectManager.deleteProject(message.getName());
                break;
            case UPDATE_VERSION:
                projectManager.updateVersion(message.getName(),message.getVersion());
                break;
            case CREATE_DIRECTORY:
                projectManager.createDirectory(message.getName(),message.getPath());
                break;
            case DELETE_DIRECTORY:
                projectManager.deleteDirectory(message.getName(),message.getPath());
                break;
            case SCREENSHOT:
                onScreenshot();
                break;
            default:
                throw new RuntimeException("unknown command");
        }
    }

    private MediaProjection mediaProjection = null;
    private Image oldImage = null;

    private synchronized ByteString getScreenshotData(){
        if (mediaProjection != null){
            onPrepareScreenshot();
            Image image;
            while (true)
            {
                image = imageReader.acquireLatestImage();
                if (image != null)
                {
                    if (oldImage != null)
                        oldImage.close();
                    oldImage = image;
                    break;
                }
                if (oldImage != null)
                {
                    image = oldImage;
                    break;
                }
                try{
                    Thread.sleep(10);
                }catch (InterruptedException e)
                {
                    return null;
                }
            }
            Image.Plane plane = image.getPlanes()[0];
            Bitmap source = Bitmap.createBitmap(
                    plane.getRowStride()/plane.getPixelStride(),
                    image.getHeight(),Bitmap.Config.ARGB_8888);
            plane.getBuffer().clear();
            source.copyPixelsFromBuffer(plane.getBuffer());
            Bitmap newMap = Bitmap.createBitmap(source,0,0,width,height);
            source.recycle();
            ByteString.Output outputStream = ByteString.newOutput();
            if (newMap.compress(Bitmap.CompressFormat.PNG,100,outputStream))
            {
                newMap.recycle();
                return outputStream.toByteString();
            }
            newMap.recycle();
        }
        return null;
    }
    private ImageReader imageReader = null;
    private VirtualDisplay virtualDisplay = null;
    private int width;
    private int height;
    @SuppressLint("WrongConstant")
    private void onInitializeScreenshot()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        imageReader = ImageReader.newInstance(width,height, PixelFormat.RGBA_8888,3);
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "screenshot",width,height,displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),null,null);
    }

    private boolean isNeedResetScreenshot()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels != width || displayMetrics.heightPixels != height;
    }

    private void onPrepareScreenshot()
    {
        if (imageReader == null)
            onInitializeScreenshot();
        else if(isNeedResetScreenshot())
        {
            releaseScreenShot();
            onInitializeScreenshot();
        }
    }

    private void releaseScreenShot()
    {
        if (oldImage != null)
        {
            oldImage.close();
            oldImage = null;
        }
        imageReader.close();
        imageReader = null;
        virtualDisplay.release();
        virtualDisplay = null;
    }

    private void onScreenshot()
    {
        DebugMessage.Message.Builder builder = DebugMessage.Message.newBuilder();
        builder.setMethod(DebugMessage.METHOD.SCREENSHOT);
        ByteString screenshotData = getScreenshotData();
        if (screenshotData != null)
            builder.setData(screenshotData);
        send(builder.build());
    }


    private void run(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        AutoLuaEngine autoLuaEngine =onCreateAutoLuaEngine();
        Thread thread = Thread.currentThread();
        DebugServerFinder finder = new DebugServerFinder(port);
        try{
            while (!thread.isInterrupted())
            {
                finder.initialize();
                finder.start();
                Socket socket = serverSocket.accept();
                finder.stop();
                try{
                    transport = new Transport(socket);
                    while (!thread.isInterrupted())
                    {
                        DebugMessage.Message message = transport.receive();
                        onHandler(autoLuaEngine, message);
                    }
                }catch (IOException e)
                {
                    autoLuaEngine.interrupt();
                }catch (Throwable e){
                    e.printStackTrace();
                } finally {
                    transport.close();
                    transport = null;
                }
            }
        }finally {
            autoLuaEngine.destroy();
            finder.stop();
            stopSelf();
        }
    }

    private AutoLuaEngine onCreateAutoLuaEngine()
    {
        RemoteLuaContextManager remoteLuaContextManager = new RemoteLuaContextManager.Builder()
                .outputPrintListener(new RemoteLuaContextManager.PrintListener() {
                    @Override
                    public void onPrint(String message) {
                        Log.d("AutoLuaEngine",message);
                    }
                })
                .errorPrintListener(new RemoteLuaContextManager.PrintListener() {
                    @Override
                    public void onPrint(String message) {
                        Log.e("AutoLuaEngine",message);
                    }
                })
                .build(this);
        AutoLuaEngine autoLuaEngine = new AutoLuaEngine(remoteLuaContextManager);
        LuaFunctionAdapter luaPrintHandler = new NotReleaseLuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                String source = luaContext.toString(1);
                int line = (int) luaContext.toLong(2);
                String message = luaContext.toString(3);
                sendLog(source,line,message);
                return 0;
            }
        };

        autoLuaEngine.addInitializeMethod(new NotReleaseLuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                byte[] code = AssetManager.read(DebugService.this,"initialize.lua");
                luaContext.loadBuffer(code,"initialize", LuaContext.CODE_TYPE.TEXT);
                luaContext.pCall(0,0,0);
                String rootPath = DebugService.this.rootPath;
                luaContext.push(rootPath);
                luaContext.setGlobal("ROOT_PATH");
                luaContext.getGlobal("package");
                luaContext.push("path");
                luaContext.push(rootPath + "/?.lua;");
                luaContext.setTable(-3);
                luaContext.pop(1);
                code = AssetManager.read(DebugService.this,"debug.lua");
                luaContext.loadBuffer(code,"debug", LuaContext.CODE_TYPE.TEXT_BINARY);
                luaContext.push(luaPrintHandler);
                luaContext.pCall(1,0,0);
                File file = new File(rootPath,"initialize.lua");
                if (file.exists() && file.isFile())
                {
                    luaContext.loadFile(file.getAbsolutePath(), LuaContext.CODE_TYPE.TEXT_BINARY);
                    luaContext.pCall(0,0,0);
                }
                return 0;
            }
        });
        autoLuaEngine.addInitializeMethod(UserInterfaceImplement.getDefault().getRegistrar());
        return autoLuaEngine;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static class DebugServerFinder{
        private final int port;
        private Thread workThread;
        private DatagramSocket server;
        private DebugServerFinder(int port)
        {
            this.port = port;
        }

        public void initialize() throws IOException
        {
            server = new DatagramSocket(port);

        }

        private void sendMyAddress(InetAddress inetAddress)
        {
            try{
                byte[] message = "I am AutoLuaClient".getBytes();
                DatagramPacket packet = new DatagramPacket(message,
                        message.length,inetAddress,port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
            }catch (IOException e)
            {
            }

        }

        public void start()
        {
            workThread = new Thread()
            {
                @Override
                public void run() {
                    byte[] bytes = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(bytes,bytes.length);
                    try{
                        while (!Thread.currentThread().isInterrupted())
                        {
                            server.receive(packet);
                            String message = new String(packet.getData(),0,packet.getLength());
                            if (message.equals("Where is AutoLuaClient"))
                            {
                                sendMyAddress(packet.getAddress());
                            }
                        }
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            workThread.start();
        }

        public void stop()
        {
            if (workThread != null)
            {
                workThread.interrupt();
                workThread = null;
            }
            if (server != null)
            {
                server.close();
                server = null;
            }
        }
    }
}