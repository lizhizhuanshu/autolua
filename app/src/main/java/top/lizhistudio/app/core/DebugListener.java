package top.lizhistudio.autoluaapp.core;

import top.lizhistudio.autolua.annotation.RPCMethod;

public interface DebugListener {
    String SERVICE_NAME = "DebugListener";
    @RPCMethod
    void onLog(String message,String path,int line);
    @RPCMethod
    void onError(String message,String path,int line);
    void onStop();
}
