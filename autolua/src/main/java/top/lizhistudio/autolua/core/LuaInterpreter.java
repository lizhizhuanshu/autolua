package top.lizhistudio.autolua.core;

import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.rpc.Callback;

public interface LuaInterpreter {
    @RPCMethod
    Object[] execute(byte[] code,String chunkName);
    @RPCMethod
    Object[] executeFile(String path);
    @RPCMethod
    boolean isRunning();
    @RPCMethod
    void reset();
    @RPCMethod
    void interrupt();
    @RPCMethod(alias = "asyncExecute",async = true)
    void execute(byte[] code,String chunkName,Callback callback);
    @RPCMethod(alias = "asyncExecuteFile",async = true)
    void executeFile(String path,Callback callback);
    @RPCMethod
    boolean setLoadScriptPath(String path);
    @RPCMethod
    boolean setLoadLibraryPath(String path);
}
