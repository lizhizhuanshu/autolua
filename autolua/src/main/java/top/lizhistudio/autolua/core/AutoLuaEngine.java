package top.lizhistudio.autolua.core;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import top.lizhistudio.autolua.rpc.ClientHandler;
import top.lizhistudio.autolua.rpc.RPCService;
import top.lizhistudio.autolua.rpc.RPCServiceCache;
import top.lizhistudio.autolua.rpc.ServiceHandler;
import top.lizhistudio.autolua.rpc.message.Request;
import top.lizhistudio.autolua.rpc.message.Response;
import top.lizhistudio.autolua.rpc.transport.ClientTransport;
import top.lizhistudio.autolua.rpc.transport.TSocket;
import top.lizhistudio.autolua.rpc.transport.Transport;

public class AutoLuaEngine {
    private static final String TAG = "AutoLuaEngine";
    private final Object stateMutex = new Object();
    private final HashSet<Observer> observers = new HashSet<>();
    private StartConfig startConfig = null;
    private final ConnectConfig connectConfig = new ConnectConfig();
    private final AtomicReference<Throwable> lastError = new AtomicReference<>(null);
    private STATE state = STATE.STOP;
    private Transport transport = null;
    private ClientHandler clientHandler = null;
    private ServiceHandler serviceHandler = null;
    private final HandlerThread receiveThread;
    private final Handler receiveHandler;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final ExecutorService executorService;
    private final RPCServiceCache rpcServiceCache;
    public AutoLuaEngine()
    {
        receiveThread = new HandlerThread("receiver@"+hashCode());
        receiveThread.start();
        receiveHandler = new Handler(receiveThread.getLooper());
        executorService = Executors.newCachedThreadPool();
        rpcServiceCache = new RPCServiceCache();
    }


    public enum STATE {
        STOP,
        STARTING,
        CONNECTING,
        RUNNING,
        STOPPING,
    }

    public void attach(Observer observer)
    {
        synchronized (stateMutex)
        {
            observers.add(observer);
        }
    }

    public void detach(Observer observer)
    {
        synchronized (stateMutex)
        {
            observers.remove(observer);
        }
    }

    public RPCService register(String name,Class<?> aInterface,Object service)
    {
        if (!aInterface.isInterface())
        {
            throw new RuntimeException("register aInterface need a interface");
        }
        return rpcServiceCache.put(name,new RPCService(aInterface,service));
    }

    public RPCService unRegister(String name)
    {
        return rpcServiceCache.remove(name);
    }



    private void update(STATE newState)
    {
        synchronized (stateMutex)
        {
            state = newState;
            for (Observer observer:observers)
            {
                observer.onUpdate(state);
            }
        }
    }



    private boolean compareAndSet(STATE except,STATE newState)
    {
        synchronized (stateMutex)
        {
            if (state == except)
            {
                update(newState);
                return true;
            }
            return false;
        }
    }

    private String getStartErrorMessage(Process process)
    {
        final Scanner scanner = new Scanner(process.getInputStream());
        while (scanner.hasNext())
        {
            if(scanner.hasNextInt())
            {
                int code = scanner.nextInt();
                if(code == 0)
                {
                    Log.e(TAG,"start AutoLuaEngine process error");
                    process.destroy();
                    break;
                }else if(code== 1) {
                    Log.d(TAG, "AutoLuaEngine started");
                    if(startConfig.isDebugPrint)
                    {
                        final Scanner scanner1 = new Scanner(process.getErrorStream());
                        new Thread(){
                            @Override
                            public void run() {
                                while (scanner.hasNext())
                                {
                                    Log.d(TAG,scanner.nextLine());
                                }
                            }
                        }.start();
                        new Thread(){
                            @Override
                            public void run() {
                                while (scanner1.hasNext())
                                {
                                    Log.e(TAG,scanner1.nextLine());
                                }
                            }
                        }.start();
                    }
                    return null;
                }else
                {
                    Log.d(TAG,String.valueOf(code));
                }
            }else
            {
                Log.d(TAG,scanner.nextLine());
            }
        }
        return  "start local AutoLuaEngine process error";
    }


    private void startProcess() throws IOException
    {
        String command = startConfig.buildStartString();
        Log.d(TAG,"AutoLuaEngine process start command :\n"+command);
        String startCommand = "";
        if (startConfig.isRoot)
            startCommand  = "su";
        Process process = Runtime.getRuntime().exec(startCommand);
        OutputStream outputStream = process.getOutputStream();
        outputStream.write(command.getBytes());
        outputStream.flush();
        String message = getStartErrorMessage(process);
        if (message != null)
            throw new RuntimeException(message);
    }

    private void connectServer() throws IOException
    {
        update(STATE.CONNECTING);
        TSocket tSocket;
        if (connectConfig.localAddress != null)
        {
            LocalSocket localSocket = new LocalSocket();
            localSocket.connect(new LocalSocketAddress(connectConfig.localAddress));
            tSocket = new TSocket.LocalTSocket(localSocket);
        }else
        {
            Socket socket = new Socket();
            InetSocketAddress address;
            if (connectConfig.netAddress == null)
                address = new InetSocketAddress(connectConfig.port);
            else
                address = new InetSocketAddress(connectConfig.netAddress,connectConfig.port);
            socket.connect(address,connectConfig.timeout);
            tSocket = new TSocket.NetSocket(socket);
        }
        tSocket.getOutputStream().writeObject(connectConfig.password);
        try{
            boolean r = (boolean)tSocket.getInputStream().readObject();
            if (!r)
                throw new RuntimeException("password error");
        }catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        transport = new ClientTransport(tSocket);
        clientHandler = new ClientHandler(transport);
        serviceHandler = new ServiceHandler(transport,rpcServiceCache);
    }

    private void serve()
    {
        update(STATE.RUNNING);
        while (isRunning.get())
        {
            Object message = transport.receive();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (message instanceof Request)
                        serviceHandler.onReceive((Request)message);
                    else if(message instanceof Response)
                        clientHandler.onReceive((Response)message);
                    else
                        throw new RuntimeException("unknown message");
                }
            });
        }
    }

    private void run() throws IOException
    {
        if (startConfig != null)
        {
            startProcess();
            connectConfig.localAddress = startConfig.localAddress;
            connectConfig.netAddress = null;
            connectConfig.port = startConfig.port;
            connectConfig.password = startConfig.password;
        }else
        {
            Log.d(TAG,"don't set start config.");
        }
        if (connectConfig.localAddress == null && connectConfig.port == -1)
        {
            throw new RuntimeException("need set connect config localAddress or port");
        }else if(connectConfig.password == null)
        {
            throw new RuntimeException("need set password");
        }
        connectServer();
        serve();
    }

    public StartConfig getStartConfig()
    {
        if (startConfig == null)
            startConfig = new StartConfig();
        return startConfig;
    }

    public void clearStartConfig()
    {
        startConfig = null;
    }

    public ConnectConfig getConnectConfig()
    {
        return connectConfig;
    }


    public STATE getState()
    {
        synchronized (stateMutex)
        {
            return state;
        }
    }

    public Throwable getLastError()
    {
        return lastError.get();
    }

    private void releaseConnect()
    {
        Transport transport = this.transport;
        if (transport != null)
            transport.close();
        this.transport = null;
        clientHandler = null;
        serviceHandler = null;
    }


    public void start()
    {
        if (compareAndSet(STATE.STOP,STATE.STARTING))
        {
            receiveHandler.post(new Runnable() {
                @Override
                public void run() {
                    lastError.set(null);
                    isRunning.set(true);
                    try{
                        AutoLuaEngine.this.run();
                    }catch (Throwable e)
                    {
                        lastError.set(e);
                    }finally {
                        releaseConnect();
                        update(STATE.STOP);
                    }
                }
            });
        }
    }

    public void stop()
    {
        if (compareAndSet(STATE.RUNNING,STATE.STOPPING))
        {
            isRunning.set(false);
            receiveThread.interrupt();
            transport.close();
            transport = null;
        }
    }

    public void sendStop()
    {
        if (compareAndSet(STATE.RUNNING,STATE.STOPPING))
        {
            clientHandler.sendExitRequest();
            isRunning.set(false);
            receiveThread.interrupt();
            transport = null;
        }
    }

    public LuaInterpreter getInterrupt()
    {
        ClientHandler clientHandler = this.clientHandler;
        try{
            if (clientHandler != null)
                return clientHandler.getService(Server.AUTO_LUA_SERVICE_NAME,LuaInterpreter.class);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        return null;
    }




    public interface Observer
    {
        void onUpdate(STATE state);
    }

    public static class ConnectConfig
    {
        private int port = -1;
        private String netAddress = null;
        private String localAddress = null;
        private String password = null;
        private int timeout = 0;

        public ConnectConfig setLocalAddress(String localAddress) {
            this.localAddress = localAddress;
            port = -1;
            netAddress = null;
            return this;
        }

        public ConnectConfig setNetAddress(String address,int port)
        {
            this.netAddress = address;
            this.port = port;
            this.localAddress = null;
            return this;
        }

        public ConnectConfig setNetAddress(String address)
        {
            this.netAddress = address;
            this.port = -1;
            this.localAddress = null;
            return this;
        }

        public ConnectConfig setTimeout(int timeout)
        {
            this.timeout = timeout;
            return this;
        }

        public ConnectConfig setPassword(String password)
        {
            this.password = password;
            return this ;
        }
    }

    public static class StartConfig
    {
        private boolean isDebugPrint = false;
        private boolean isRoot = true;
        private boolean isUse64bit = false;
        private String niceName = null;
        private String scriptPath = null;
        private String password = null;
        private String localAddress = null;
        private int port = -1;
        private String packagePath = null;
        private Class<?> luaInterpreterFactory = null;

        private StartConfig(){}

        private String getLibraryPath()
        {
            return packagePath.substring(0,packagePath.lastIndexOf('/')+1)+"lib/";
        }

        private boolean isCanUse64Bit()
        {
            String libraryPath = getLibraryPath();
            File file = new File(libraryPath);
            for(String s:file.list())
            {
                if(s.indexOf("64")>0)
                {
                    return true;
                }
            }
            return false;
        }

        public StartConfig setProcessPrint(boolean is)
        {
            isDebugPrint = is;
            return this;
        }


        public StartConfig setRoot(boolean is)
        {
            this.isRoot = is;
            return this;
        }

        public StartConfig setPackagePath(String packagePath)
        {
            this.packagePath = packagePath;
            return this;
        }

        public StartConfig setUse64Bit()
        {
            this.isUse64bit = isCanUse64Bit();
            return this;
        }

        public StartConfig setNiceName(String name)
        {
            niceName = name;
            return this;
        }

        public StartConfig setScriptPath(String path)
        {
            scriptPath = path;
            return this;
        }

        public StartConfig setPassword(String password)
        {
            this.password = password;
            return this;
        }

        public StartConfig setLocalAddress(String localAddress)
        {
            this.localAddress = localAddress;
            return this;
        }

        public StartConfig setServiceAddress(int port)
        {
            this.port = port;
            return this;
        }

        public StartConfig setLuaInterpreterFactory(Class<? extends LuaInterpreterFactory> aClass)
        {
            this.luaInterpreterFactory = aClass;
            return this;
        }


        public static String getRandomString(int length){
            String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random=new Random();
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<length;i++){
                int number=random.nextInt(62);
                sb.append(str.charAt(number));
            }
            return sb.toString();
        }

        private void append(StringBuilder builder,String arg)
        {
            builder.append("  ").append(arg);
        }

        private void appendArg(StringBuilder builder, String k,String v)
        {
            append(builder,k);
            append(builder,v);
        }

        private String buildStartString()
        {
            StringBuilder command = new StringBuilder();
            String libPathHead = getLibraryPath();
            File file = new File(libPathHead);
            String libPath = null;

            if(isUse64bit)
            {
                for(String s:file.list()) {
                    if (s.indexOf("64") > 0) {
                        libPath = libPathHead + s;
                        break;
                    }
                }
            }else
                libPath = libPathHead + file.list()[0];


            if(scriptPath != null)
            {
                command.append("export LUA_PATH=\"")
                        .append(scriptPath)
                        .append("\"\n");
            }

            command.append("export LUA_CPATH=\"")
                    .append( libPath)
                    .append("/lib?.so;")
                    .append(libPath)
                    .append("/?/init.so")
                    .append("\"\n");

            command.append("export LD_LIBRARY_PATH=\"")
                    .append(libPath)
                    .append(":" )
                    .append(System.getProperty("java.library.path"))
                    .append("\"\n");

            command.append("export CLASSPATH=")
                    .append(packagePath)
                    .append("\n");

            if(isUse64bit ||(libPath != null && libPath.contains("64")) )
                command.append("/system/bin/app_process64 ");
            else
                command.append("/system/bin/app_process32 ");

            command.append("/system/bin ");

            if(niceName != null)
            {
                command.append("--nice-name=");
                command.append(niceName);
            }

            append(command, Server.class.getName());
            if (password  == null)
                password = getRandomString(16);

            if (port >-1)
                appendArg(command,"-p",String.valueOf(port));
            else{
                if (localAddress == null)
                {
                    localAddress = getRandomString(16);
                    Log.d(TAG,"don't set localAddress ,use random "+localAddress);
                }

                appendArg(command,"-l",localAddress);
            }
            appendArg(command,"-v",password);
            if (luaInterpreterFactory == null)
                throw new RuntimeException("need set LuaContextFactory");
            appendArg(command,"-f","'"+ luaInterpreterFactory.getName()+"'");
            command.append('\n');
            return command.toString();
        }
    }
}
