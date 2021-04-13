package top.lizhistudio.app.core;

import android.net.Uri;

import java.io.Serializable;
import java.util.Set;

import top.lizhistudio.autolua.debugger.proto.Date;

public interface ProjectManager {

    ProjectInfo getInfo(String projectName);
    void addProject(String path);
    boolean createProject(String projectName,String feature,int version);
    boolean createDirectory(String projectName,String path);
    boolean updateVersion(String projectName,int version);
    boolean updateFile(String projectName,String path,byte[] data);
    boolean deleteFile(String projectName,String path);
    boolean deleteDirectory(String projectName,String path);
    boolean deleteProject(String projectName);
    String getProjectPath(String projectName);
    void attach(Observer observer);
    void detach(Observer observer);
    Set<String> allProjectName();


    enum Event
    {
        DELETE,Add
    }

    interface Observer
    {
        void onUpdate(String projectName,Event event);
    }


    class ProjectInfo implements Serializable
    {
        public String name;
        public String feature;
        public int version;
        public ProjectInfo(String name,String feature,int version)
        {
            this.name = name;
            this.feature = feature;
            this.version = version;
        }

        public ProjectInfo(Date.ProjectInfoData data)
        {
            name = data.getName();
            feature = data.getFeature();
            version = data.getVersion();
        }
    }
}
