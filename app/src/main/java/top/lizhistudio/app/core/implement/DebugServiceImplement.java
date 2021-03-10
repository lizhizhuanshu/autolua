package top.lizhistudio.app.core.implement;

import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import top.lizhistudio.app.core.DebugListener;
import top.lizhistudio.app.core.ProjectManager;
import top.lizhistudio.app.thrift.DebuggerService;
import top.lizhistudio.app.thrift.MESSAGE_TYPE;
import top.lizhistudio.app.thrift.Message;
import top.lizhistudio.app.thrift.ProjectInfo;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.rpc.Callback;

public class DebugServiceImplement implements DebuggerService.Iface {
    private final ProjectManager projectManager;
    private final LinkedBlockingQueue<Message> messages;
    private final DebugListener listener;
    private void sendMessage(Message message) {
        try{
            messages.put(message);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public DebugServiceImplement()
    {
        projectManager = ProjectManagerImplement.getInstance();
        messages = new LinkedBlockingQueue<>();
        listener = new DebugListener() {
            @Override
            public void onLog(String message, String path, int line) {
                sendMessage(new Message(MESSAGE_TYPE.LOG,message,path,line));
            }

            @Override
            public void onStop() {
                sendMessage(new Message(MESSAGE_TYPE.STOP,null,null,-1));
            }

            @Override
            public void onError(String message, String path, int line) {
                sendMessage(new Message(MESSAGE_TYPE.ERROR,message,path,line));
            }
        };
    }

    @Override
    public ProjectInfo getInfo(String projectName) throws TException {
        ProjectManager.ProjectInfo r = projectManager.getInfo(projectName);
        ProjectInfo result = new ProjectInfo();
        result.name = r.name;
        result.feature = r.feature;
        result.version = r.version;
        return result;
    }

    @Override
    public boolean createProject(String projectName, String feature, long version) throws TException {
        return projectManager.createProject(projectName,feature,version);
    }


    @Override
    public boolean createDirectory(String projectName, String path) throws TException {
        return projectManager.createDirectory(projectName,path);
    }

    @Override
    public boolean updateVersion(String projectName, long version) throws TException {
        return projectManager.updateVersion(projectName,version);
    }

    @Override
    public boolean updateFile(String projectName, String path, ByteBuffer data) throws TException {
        return projectManager.updateFile(projectName,path,data.array());
    }

    @Override
    public boolean deleteFile(String projectName, String path) throws TException {
        return projectManager.deleteFile(projectName,path);
    }

    @Override
    public boolean deleteDirectory(String projectName, String path) throws TException {
        return projectManager.deleteDirectory(projectName,path);
    }

    @Override
    public boolean deleteProject(String projectName) throws TException {
        return projectManager.deleteProject(projectName);
    }

    @Override
    public boolean executeFile(String projectName, String path) throws TException {
        LuaInterpreter interpreter = AutoLuaEngine.getInstance().getInterrupt();
        if (interpreter != null && !interpreter.isRunning())
        {
            String projectPath = projectManager.getProjectPath(projectName);
            if (projectPath!= null)
            {
                interpreter.reset();
                interpreter.setLoadScriptPath(projectPath);
                AutoLuaEngine.getInstance().register(DebugListener.SERVICE_NAME,
                        DebugListener.class,
                        listener);
                interpreter.executeFile(projectPath + "/" + path, new Callback() {
                    @Override
                    public void onCompleted(Object result) {
                        listener.onStop();
                        AutoLuaEngine.getInstance().unRegister(DebugListener.SERVICE_NAME);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        listener.onStop();
                        AutoLuaEngine.getInstance().unRegister(DebugListener.SERVICE_NAME);
                    }
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public void interrupt() throws TException {
        LuaInterpreter interpreter = AutoLuaEngine.getInstance().getInterrupt();
        if (interpreter != null && interpreter.isRunning())
            interpreter.interrupt();
    }

    @Override
    public Message getMessage() throws TException {
        try{
            return messages.take();
        }catch (InterruptedException e)
        {
            return new Message(MESSAGE_TYPE.ERROR,e.getMessage(),"unknown",-1);
        }
    }

}
