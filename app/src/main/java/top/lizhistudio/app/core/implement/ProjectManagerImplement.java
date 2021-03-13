package top.lizhistudio.app.core.implement;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import top.lizhistudio.autolua.debugger.proto.Date;
import top.lizhistudio.app.core.ProjectManager;

public class ProjectManagerImplement implements ProjectManager {
    private String rootPath = null;
    private String projectPath;
    private String configPath;
    private final HashMap<String,ProjectInfo> projectInfoCache;
    private final HashSet<Observer> observers;
    private ProjectManagerImplement()
    {
        projectInfoCache = new HashMap<>();
        observers = new HashSet<>();
    }

    private void update(Event event,String projectName)
    {
        synchronized (observers)
        {
            for (Observer o :
                    observers) {
                o.onUpdate(projectName, event);
            }
        }
    }


    public synchronized void initialize(Context context)
    {
        if (rootPath != null)
            return;
        rootPath = context.getFilesDir().getAbsolutePath();
        projectPath = rootPath + "/"+"projects";
        configPath = rootPath + "/"+"config";
        try{
            File file = new File(projectPath);
            if (!file.exists())
            {
                if (!file.mkdir())
                {
                    throw new RuntimeException("create projects error");
                }
            }else if(!file.isDirectory())
            {
                throw new RuntimeException("projects need directory");
            }
            File config = new File(configPath);
            if (!config.exists())
            {
                if(!config.createNewFile())
                    throw new RuntimeException("create project config error");
                return;
            }else if (!config.isFile())
            {
                throw new RuntimeException("config need file");
            }
            FileInputStream inputStream = new FileInputStream(config);
            Date.ProjectInfoCache cache = Date.ProjectInfoCache.parseDelimitedFrom(inputStream);
            inputStream.close();
            if (cache == null)
                return;
            synchronized (projectInfoCache)
            {
                for (int i=0;i<cache.getDataCount();i++)
                {
                    Date.ProjectInfoData data = cache.getData(i);
                    projectInfoCache.put(data.getName(),new ProjectInfo(data));
                }
            }
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }



    @Override
    public ProjectInfo getInfo(String projectName) {
        synchronized (projectInfoCache)
        {
            return projectInfoCache.get(projectName);
        }
    }


    private void save()
    {
        synchronized (projectInfoCache)
        {
            Date.ProjectInfoCache.Builder builder = Date.ProjectInfoCache.newBuilder();
            for (ProjectInfo info:projectInfoCache.values())
            {
                builder.addData(Date.ProjectInfoData.newBuilder().setName(info.name)
                        .setFeature(info.feature)
                        .setVersion(info.version).build());
            }
            Date.ProjectInfoCache cache = builder.build();
            try{
                File file = new File(configPath);
                FileOutputStream outputStream = new FileOutputStream(file);
                cache.writeDelimitedTo(outputStream);
                outputStream.flush();
                outputStream.close();
            }catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
    }



    @Override
    public boolean createProject(String projectName, String feature,long version) {
        synchronized (projectInfoCache)
        {
            ProjectInfo info = projectInfoCache.get(projectName);
            if (info != null)
                return false;
            projectInfoCache.put(projectName,new ProjectInfo(projectName,feature,version));
            save();
        }
        update(Event.Add,projectName);
        return true;
    }

    @Override
    public boolean createDirectory(String projectName, String path) {
        ProjectInfo info = getInfo(projectName);
        if (info == null)
            return false;
        File file = new File(projectPath+"/"+projectName,path);

        if (file.exists())
            return false;
        return file.mkdirs();
    }

    @Override
    public boolean updateVersion(String projectName, long version) {
        ProjectInfo info = getInfo(projectName);
        if (info == null)
            return false;
        info.version = version;
        return true;
    }

    @Override
    public boolean updateFile(String projectName, String path, byte[] data) {
        ProjectInfo info = getInfo(projectName);
        if (info == null)
            return false;
        File file = new File(projectPath+"/"+projectName,path);
        if (file.isDirectory())
            return false;
        try{
            if (!file.exists())
            {
                if (!file.createNewFile())
                    return false;
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
            return true;
        }catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteFile(String projectName, String path) {
        ProjectInfo info = getInfo(projectName);
        if (info == null)
            return false;
        File file = new File(projectPath+"/"+projectName,path);
        if (file.isDirectory())
            return false;
        if (file.isFile())
            return file.delete();
        return true;
    }

    @Override
    public boolean deleteDirectory(String projectName, String path) {
        ProjectInfo info = getInfo(projectName);
        if (info == null)
            return false;
        File file = new File(projectPath+"/"+projectName,path);
        if (file.isFile())
            return false;
        if (file.isDirectory())
            return file.delete();
        return true;
    }

    @Override
    public boolean deleteProject(String projectName) {
        synchronized (projectInfoCache)
        {
            ProjectInfo info = projectInfoCache.get(projectName);
            if (info == null)
                return true;
            File file = new File(projectPath,projectName);
            if (file.exists())
                file.delete();
            projectInfoCache.remove(projectName);
            save();
        }
        update(Event.DELETE,projectName);
        return true;
    }

    @Override
    public String getProjectPath(String projectName) {
        ProjectInfo info = getInfo(projectName);
        if (info == null)
            return null;
        return projectPath +"/"+projectName;
    }

    @Override
    public void attach(Observer observer) {
        synchronized (observers)
        {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        synchronized (observers)
        {
            observers.remove(observer);
        }
    }

    @Override
    public Set<String> allProjectName() {
        synchronized (projectInfoCache)
        {
            return projectInfoCache.keySet();
        }
    }

    private static class Stub
    {
        private final static ProjectManagerImplement stub = new ProjectManagerImplement();
    }

    public static ProjectManagerImplement getInstance()
    {
        return Stub.stub;
    }
}
