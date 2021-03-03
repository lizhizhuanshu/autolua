package top.lizhistudio.autolua;

import top.lizhistudio.androidlua.annotation.LuaMethod;

public interface LuaInterpreter {
    @LuaMethod
    Object[] execute(byte[] code,String chunkName);
    @LuaMethod
    Object[] executeFile(String path);
    @LuaMethod
    boolean isRunning();
    @LuaMethod
    void reset();
    @LuaMethod
    void interrupt();
    void execute(byte[] code,String chunkName,Callback callback);
    void executeFile(String path,Callback callback);
    interface Callback{
        void onCompleted(Object[] result);
        void onError(Throwable throwable);
    }
}
