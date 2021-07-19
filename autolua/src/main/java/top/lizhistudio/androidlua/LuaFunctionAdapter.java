package top.lizhistudio.androidlua;

public interface LuaFunctionAdapter extends LuaAdapter{
    int onExecute(LuaContext luaContext) throws Throwable;
}
