package top.lizhistudio.androidlua;

public interface LuaObjectAdapter {
    boolean hasMethod(String name);
    int call(String methodName, LuaContext luaContext) throws Throwable;
    Object getJavaObject();
}
