package top.lizhistudio.androidlua;

public interface LuaFunctionAdapter {
    int onExecute(LuaContext luaContext) throws Throwable;
}
