package top.lizhistudio.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.immomo.mls.MLSEngine;
import com.immomo.mls.global.LVConfigBuilder;

import java.io.File;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.NotReleaseLuaFunctionAdapter;
import top.lizhistudio.app.core.UserInterfaceImplement;
import top.lizhistudio.app.util.AssetManager;
import top.lizhistudio.app.view.FloatControllerView;
import top.lizhistudio.app.view.FloatControllerViewImplement;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.core.RemoteLuaContextManager;
import top.lizhistudio.autolua.core.value.LuaValue;

public class AutoLuaService extends Service {
    private static final int NOTIFICATION_ID = 10087;
    private static final String CHANNEL_ONE_ID = "AutoLua";
    private String rootPath = null;
    private AutoLuaEngine autoLuaEngine = null;
    private FloatControllerViewImplement floatControllerView = null;
    public AutoLuaService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onStartNotification();
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
                .setContentText("AutoLua服务正在运行")
                .setSmallIcon(R.mipmap.auxiliary)
                .setWhen(System.currentTimeMillis());

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ONE_ID);

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(NOTIFICATION_ID,notification);
    }

    private boolean isStarted = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isStarted)
        {
            isStarted = true;
            rootPath = intent.getStringExtra("projectPath");
            if (!rootPath.equals(""))
            {
                onStart();
            }else
            {
                Toast.makeText(this,"AutoLua服务器启动失败，项目路径不能为空",Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void onStart()
    {

        MLSEngine.setLVConfig(new LVConfigBuilder(AutoLuaService.this)
                .setRootDir(rootPath)
                .setImageDir(rootPath+"/image")
                .setCacheDir(AutoLuaService.this .getCacheDir().getAbsolutePath())
                .setGlobalResourceDir(rootPath+"/resource").build());
        autoLuaEngine = onCreateAutoLuaEngine();
        floatControllerView = new FloatControllerViewImplement(this,40);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        registerReceiver(floatControllerView,filter);
        LuaInterpreter.Callback callback = new LuaInterpreter.Callback() {
            @Override
            public void onCallback(LuaValue[] result) {
                floatControllerView.setState(FloatControllerView.STOPPED_STATE);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                floatControllerView.setState(FloatControllerView.STOPPED_STATE);
            }
        };

        floatControllerView.setOnClickListener(new FloatControllerView.OnClickListener() {
            @Override
            public void onClick(FloatControllerView floatControllerView, int state) {
                if (state == FloatControllerView.EXECUTEING_STATE)
                    autoLuaEngine.interrupt();
                else
                {
                    floatControllerView.setState(FloatControllerView.EXECUTEING_STATE);
                    autoLuaEngine.executeFile(rootPath + "/main.lua", LuaContext.CODE_TYPE.TEXT,callback);
                }
            }
        });
        floatControllerView.reShow();
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
        autoLuaEngine.addInitializeMethod(new NotReleaseLuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                byte[] code = AssetManager.read(AutoLuaService.this,"initialize.lua");
                luaContext.loadBuffer(code,"initialize", LuaContext.CODE_TYPE.TEXT);
                luaContext.pCall(0,0,0);
                String rootPath = AutoLuaService.this.rootPath;
                luaContext.push(rootPath);
                luaContext.setGlobal("ROOT_PATH");
                luaContext.getGlobal("package");
                luaContext.push("path");
                luaContext.push(rootPath + "/?.lua;");
                luaContext.setTable(-3);
                luaContext.pop(1);
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
    public void onDestroy() {
        super.onDestroy();
        autoLuaEngine.destroy();
        unregisterReceiver(floatControllerView);
        floatControllerView.conceal();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}