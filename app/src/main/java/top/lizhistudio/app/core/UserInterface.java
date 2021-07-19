package top.lizhistudio.app.core;

import android.view.WindowManager;

import top.lizhistudio.autolua.core.value.LuaValue;


public interface UserInterface {
    void showMessage(String message,int time);
    FloatView newFloatView(String name, String uri, WindowManager.LayoutParams layoutParams);
    LuaValue[] takeSignal() throws InterruptedException;
    FloatView getFloatView(String name);
    void putSignal(LuaValue[] message) throws InterruptedException;

    interface FloatView
    {
        void show();
        void conceal();
        void setXY(int x,int y);
        void setWidthHeight(int width,int height);
        int getX();
        int getY();
        int getWidth();
        int getHeight();
        boolean reload(String uri);
        void destroy();
        String getName();
    }
}
