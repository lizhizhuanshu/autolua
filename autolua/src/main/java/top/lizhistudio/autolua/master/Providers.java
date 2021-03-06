package top.lizhistudio.autolua.master;

import top.lizhistudio.androidlua.annotation.LuaMethod;

public interface CodeProviders {
    @LuaMethod
    byte[] getCode(String path);
}
