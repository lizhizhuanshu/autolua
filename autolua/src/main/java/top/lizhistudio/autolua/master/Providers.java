package top.lizhistudio.autolua.master;

import top.lizhistudio.androidlua.annotation.LuaMethod;

public interface Providers {
    @LuaMethod
    byte[] getCode(String path);
    @LuaMethod
    Object getConfig(String key);
}
