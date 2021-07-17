package top.lizhistudio.app.core.debugger;



import com.immomo.mls.MLSEngine;
import com.immomo.mls.global.LVConfigBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.Util;
import top.lizhistudio.app.App;
import top.lizhistudio.app.core.ProjectManager;
import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.core.value.LuaValue;
import top.lizhistudio.autolua.debugger.proto.DebugMessage;

public class DebuggerServer extends Observable {
    private static final String DEBUGGER_NAME = "debugger";
    private static final String TAG = "DebuggerServer";
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile ServerSocket serverSocket;
    private volatile Transport transport;
    private String rootPath = null;
    private LuaFunctionAdapter debuggerInitializeHandler = null;

    private volatile String errorMessage = null;


    private String relativePath(String rootPath,String scriptPath)
    {
        if (scriptPath.charAt(0) == '@')
        {
            return scriptPath.substring(rootPath.length()+2);
        }
        return scriptPath;
    }

    private DebuggerServer()
    {
        LuaFunctionAdapter luaPrintHandler = new LuaFunctionAdapter() {
            private String getMessage(LuaContext context)
            {
                StringBuilder builder = new StringBuilder();
                for (int i=1;i<=context.getTop();i++)
                {
                    builder.append(Util.luaValueToString(context,i));
                    if (i!=context.getTop())
                    {
                        builder.append("    ");
                    }
                }
                return builder.toString();
            }

            private void onLog(String source,int line,String message)
            {
                send(DebugMessage.Message.newBuilder()
                        .setMethod(DebugMessage.METHOD.LOG)
                        .setPath(relativePath(rootPath,source))
                        .setLine(line)
                        .setMessage(message).build());
            }

            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                String message = getMessage(luaContext);
                luaContext.getGlobal("debug");
                luaContext.push("getinfo");
                luaContext.getTable(-2);
                luaContext.push(2);
                luaContext.push("Sl");
                luaContext.pCall(2,1,0);
                luaContext.push("source");
                luaContext.getTable(-2);
                String source = luaContext.toString(-1);
                luaContext.pop(1);
                luaContext.push("currentline");
                luaContext.getTable(-2);
                int line = (int) luaContext.toLong(-1);
                onLog(source,line,message);
                return 0;
            }
        };


        debuggerInitializeHandler= new LuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                luaContext.push(luaPrintHandler);
                luaContext.setGlobal("print");
                return 0;
            }
        };
    }



    public boolean isServing()
    {
        return isRunning.get();
    }


    private void update(boolean isRun)
    {
        synchronized (isRunning)
        {
            isRunning.set(isRun);
            setChanged();
            notifyObservers(isRun);
        }
    }

    private boolean compareAndSet(boolean exception,boolean set)
    {
        synchronized (isRunning)
        {
            if (isRunning.compareAndSet(exception,set))
            {

                setChanged();
                notifyObservers(set);
                return true;
            }
        }
        return false;
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


    private void onExecuteFile(String projectName,String path)
    {
        ProjectManager projectManager = ProjectManagerImplement.getInstance();
        AutoLuaEngine interpreter = App.getApp().getAutoLuaEngine();
        if (interpreter != null && !interpreter.isRunning())
        {
            String projectPath = projectManager.getProjectPath(projectName);
            if (projectPath!= null)
            {
                MLSEngine.setLVConfig(new LVConfigBuilder(App.getApp())
                        .setRootDir(projectPath)
                        .setImageDir(projectPath+"/image")
                        .setCacheDir(App.getApp().getCacheDir().getAbsolutePath())
                        .setGlobalResourceDir(projectPath+"/resource").build());
                //此处需要注意
                rootPath = projectPath;
                interpreter.destroyNowLuaContext();
                interpreter.addInitializeHandler(debuggerInitializeHandler);
                App.getApp().setScriptLoadPath(projectPath);
                interpreter.executeFile(projectPath + "/" + path,
                        LuaContext.CODE_TYPE.TEXT_BINARY,new LuaInterpreter.Callback() {
                            @Override
                            public void onCallback(LuaValue[] result) {
                                sendStopped();
                                interpreter.removeInitializeHandler(debuggerInitializeHandler);
                            }
                            @Override
                            public void onError(Throwable throwable) {
                                sendStopped();
                                if (throwable != null)
                                {
                                    throwable.printStackTrace();
                                }
                                interpreter.removeInitializeHandler(debuggerInitializeHandler);
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


    private void onHandler(DebugMessage.Message message)
    {
        AutoLuaEngine autoLuaEngine = App.getApp().getAutoLuaEngine();
        ProjectManager projectManager = ProjectManagerImplement.getInstance();
        switch (message.getMethod())
        {
            case UNKNOWN:
                break;
            case INTERRUPT:
                autoLuaEngine.interrupt();
                break;
            case EXECUTE_FILE:
                onExecuteFile(message.getName(),message.getPath());
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
            default:
                throw new RuntimeException("unknown command");
        }
    }


    private void run(int port) throws IOException
    {
        try{
            serverSocket = new ServerSocket(port);
            while (isRunning.get())
            {
                Socket socket = serverSocket.accept();
                try{
                    transport = new Transport(socket);
                    while (isRunning.get())
                    {
                        DebugMessage.Message message = transport.receive();
                        onHandler(message);
                    }
                }catch (IOException e)
                {
                    e.printStackTrace();
                    App.getApp().getAutoLuaEngine().interrupt();
                }catch (Throwable e){
                    e.printStackTrace();
                } finally {
                    transport.close();
                    transport = null;
                }
            }
        }finally {
            errorMessage = "服务器启动失败";
        }
        errorMessage = null;
    }



    private void startService(int port)
    {
        new Thread()
        {
            @Override
            public void run() {
                try{
                    DebuggerServer.this.run(port);
                }catch (Throwable e)
                {
                    e.printStackTrace();
                }
                finally {
                    update(false);
                }
            }
        }.start();
    }


    public void start(int port)
    {
        if (compareAndSet(false,true))
        {
            startService(port);
        }
    }

    public void stop()
    {
        if (isRunning.get())
        {
            if (serverSocket != null)
            {
                try{
                    serverSocket.close();
                    serverSocket = null;
                }catch (IOException e)
                {
                }
            }
        }
    }

    private static class Stub{
        private static final DebuggerServer server = new DebuggerServer();
    }
    public static DebuggerServer getInstance()
    {
        return Stub.server;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

}
