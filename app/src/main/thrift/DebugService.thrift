namespace java top.lizhistudio.debugger.thrift

struct ProjectInfo{
    1:string name;
    2:string feature;
    3:i64 version;
}

enum MESSAGE_TYPE{
    LOG =1;
    ERROR = 2;
    STOP = 3;
}

struct Message{
    1:MESSAGE_TYPE type;
    2:string message;
    3:string path;
    4:i32 line;
}


service DebuggerService{
    ProjectInfo getInfo(1:string projectName);
    bool createProject(1:string projectName,2: string feature 3: i64 version);
    bool createDirectory(1:string projectName,2: string path)
    bool updateVersion(1:string projectName,2: i64 version);
    bool updateFile(1:string projectName,2: string path,3: binary data);
    bool deleteFile(1:string projectName,2: string path);
    bool deleteDirectory(1:string projectName,2: string path);
    bool deleteProject(1:string projectName);
    bool executeFile(1:string projectName,2: string path);
    void interrupt();
    Message getMessage();
}