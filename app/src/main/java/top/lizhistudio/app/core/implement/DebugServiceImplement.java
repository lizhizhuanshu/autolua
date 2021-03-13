package top.lizhistudio.app.core.implement;

import android.util.Log;

import com.immomo.mls.MLSEngine;
import com.immomo.mls.global.LVConfigBuilder;

import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.app.App;
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
    private static final String TAG = "DebugService";
    private final ProjectManager projectManager;
    private final LinkedBlockingQueue<Message> messages;
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
    }

    @Override
    public ProjectInfo getInfo(String projectName) throws TException {
        //Log.d(TAG,"call getInfo");
        ProjectManager.ProjectInfo r = projectManager.getInfo(projectName);
        ProjectInfo result = new ProjectInfo();
        if (r == null)
            return result;
        result.name = r.name;
        result.feature = r.feature;
        result.version = r.version;
        return result;
    }

    @Override
    public boolean createProject(String projectName, String feature, long version) throws TException {
        //Log.d(TAG,"create project");
        return projectManager.createProject(projectName,feature,version);
    }


    @Override
    public boolean createDirectory(String projectName, String path) throws TException {
        //Log.d(TAG,"create directory");
        return projectManager.createDirectory(projectName,path);
    }

    @Override
    public boolean updateVersion(String projectName, long version) throws TException {
        //Log.d(TAG,"update version");
        return projectManager.updateVersion(projectName,version);
    }

    @Override
    public boolean updateFile(String projectName, String path, ByteBuffer data) throws TException {
        //Log.d(TAG,"update file "+path);
        return projectManager.updateFile(projectName,path,
                Arrays.copyOfRange(data.array(),data.position(),data.limit()));
    }

    @Override
    public boolean deleteFile(String projectName, String path) throws TException {
        //Log.d(TAG,"delete file");
        return projectManager.deleteFile(projectName,path);
    }

    @Override
    public boolean deleteDirectory(String projectName, String path) throws TException {
        //Log.d(TAG,"delete directory");
        return projectManager.deleteDirectory(projectName,path);
    }

    @Override
    public boolean deleteProject(String projectName) throws TException {
        //Log.d(TAG,"delete project");
        return projectManager.deleteProject(projectName);
    }

    private String relativePath(String rootPath,String scriptPath)
    {
        if (scriptPath.charAt(0) == '@')
        {
            return scriptPath.substring(rootPath.length()+2);
        }
        return scriptPath;
    }

    private DebugListener newListener(String rootPath)
    {

        return new DebugListener() {
            private final AtomicBoolean isStopped = new AtomicBoolean(false);
            @Override
            public void onLog(String message, String path, int line) {
                //Log.d(TAG,"receive message "+message+" ");
                sendMessage(new Message(MESSAGE_TYPE.LOG,message,relativePath(rootPath,path),line));
            }

            @Override
            public void onStop() {
                //Log.d(TAG,"send stop ");
                if (isStopped.compareAndSet(false,true))
                {
                    sendMessage(new Message(MESSAGE_TYPE.STOP,null,null,-1));
                }

            }

            @Override
            public void onError(String message, String path, int line) {
                //Log.d(TAG,"send error ");
                if (isStopped.compareAndSet(false,true))
                {
                    if (path != null)
                        path = relativePath(rootPath,path);
                    sendMessage(new Message(MESSAGE_TYPE.ERROR,message,path,line));
                }
            }
        };
    }


    @Override
    public boolean executeFile(String projectName, String path) throws TException {
        LuaInterpreter interpreter = AutoLuaEngine.getInstance().getInterrupt();
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

                DebugListener listener = newListener(projectPath);
                //此处需要注意
                AutoLuaEngine.getInstance().register(DebugListener.SERVICE_NAME,
                        DebugListener.class,
                        listener);
                interpreter.reset();
                interpreter.setLoadScriptPath(projectPath);
                interpreter.executeFile(projectPath + "/" + path, new Callback() {
                    @Override
                    public void onCompleted(Object result) {
                        //Log.d(TAG,result.toString());
                        listener.onStop();
                        AutoLuaEngine.getInstance().unRegister(DebugListener.SERVICE_NAME);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        listener.onStop();
                        if (throwable != null)
                        {
                            throwable.printStackTrace();
                        }
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
        //Log.d(TAG,"interrupt");
        LuaInterpreter interpreter = AutoLuaEngine.getInstance().getInterrupt();
        if (interpreter != null && interpreter.isRunning())
            interpreter.interrupt();
    }

    @Override
    public Message getMessage() throws TException {
        //Log.d(TAG,"get message");
        try{
            return messages.take();
        }catch (InterruptedException e)
        {
            return new Message(MESSAGE_TYPE.ERROR,e.getMessage(),"unknown",-1);
        }finally {
            //Log.d(TAG,"take a message");
        }
    }

}
