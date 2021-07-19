package top.lizhistudio.androidlua;

import java.util.List;

public interface LuaObjectAdapter extends LuaAdapter{
    boolean hasMethod(String name);
    List<String> getAllMethodName();
    int call(String methodName, LuaContext luaContext) throws Throwable;
    Object getJavaObject();
}
