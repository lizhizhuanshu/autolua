package top.lizhistudio.autolua.ui;

import android.graphics.Point;

import top.lizhistudio.androidlua.annotation.LuaMethod;

public interface FloatView {
    @LuaMethod
    void destroy();
    @LuaMethod
    void show();
    @LuaMethod
    void conceal();
    @LuaMethod
    Point getXY();
    @LuaMethod
    void setXY(int x,int y);
    long getID();
}
