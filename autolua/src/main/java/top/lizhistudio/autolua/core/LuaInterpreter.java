package top.lizhistudio.autolua.core;

import top.lizhistudio.androidlua.LuaContext;

public interface LuaInterpreter {
    LuaValue[] execute(byte[] code, String chunkName, LuaContext.CODE_TYPE code_type);
    LuaValue[] executeFile(String path, LuaContext.CODE_TYPE code_type);
    void executeFile(String filePath, LuaContext.CODE_TYPE code_type, Callback callback);
    void execute(byte[] code, String chunkName, LuaContext.CODE_TYPE code_type, Callback callback);
    boolean isRunning();
    void interrupt();
    void destroyNowLuaContext();
    interface Callback
    {
        void onCallback(LuaValue[] result);
        void onError(Throwable throwable);
    }
}

