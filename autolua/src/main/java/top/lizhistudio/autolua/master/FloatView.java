package top.lizhistudio.autolua.master;

import top.lizhistudio.androidlua.annotation.LuaMethod;
import top.lizhistudio.autolua.extend.Point;

public interface FloatView {
    @LuaMethod
    void destroy();
    @LuaMethod
    void show();
    @LuaMethod
    void conceal();
    @LuaMethod
    int getX();
    @LuaMethod
    int getY();
    @LuaMethod
    void setXY(int x, int y);
    long getID();
}
