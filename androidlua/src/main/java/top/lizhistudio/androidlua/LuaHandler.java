package top.lizhistudio.androidlua;

public interface LuaHandler {
    int onExecute(LuaContext luaContext) throws Throwable;
}
