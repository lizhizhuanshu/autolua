package top.lizhistudio.autolua.master;

import top.lizhistudio.androidlua.annotation.LuaMethod;

public interface UI {
    @LuaMethod
    FloatView newFloatView(int width, int height, int x, int y, String path);
    @LuaMethod
    FloatView getFloatView(long id);
    @LuaMethod
    Object[] getMessage();
}
