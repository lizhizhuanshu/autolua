package top.lizhistudio.autolua.core;

import top.lizhistudio.androidlua.LuaFunctionAdapter;

public interface LuaContextManager extends LuaContextFactory{
    boolean addInitializeMethod(LuaFunctionAdapter method);
    boolean removeInitializeMethod(LuaFunctionAdapter method);
}
