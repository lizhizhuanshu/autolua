package top.lizhistudio.autolua.core;

import android.view.WindowManager;

import java.io.Serializable;
import java.lang.reflect.Field;

import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;

public interface UserInterface {
    String LUA_CLASS_NAME = "UI";
    @RPCMethod
    void showMessage(String message,int time);
    @RPCMethod
    FloatView newFloatView(String name, String uri,LayoutParams layoutParams);
    @RPCMethod
    Object takeSignal() throws InterruptedException;
    @RPCMethod
    FloatView getFloatView(String name);
    void putSignal(Object message) throws InterruptedException;

    @RPCInterface
    interface FloatView
    {
        @RPCMethod
        void show();
        @RPCMethod
        void conceal();
        @RPCMethod
        void setXY(int x,int y);
        @RPCMethod
        void setWidthHeight(int width,int height);
        @RPCMethod
        int getX();
        @RPCMethod
        int getY();
        @RPCMethod
        int getWidth();
        @RPCMethod
        int getHeight();
        @RPCMethod
        boolean reload(String uri);
        @RPCMethod
        void destroy();
        @RPCMethod
        String getName();
    }

    class LayoutParams implements Serializable
    {
        private static final long serialVersionUID = 54634563L;
        public int x;
        public int y;
        public int width=-1;
        public int height=-1;
        public int format;
        public int flags;
        public int gravity;
        public WindowManager.LayoutParams toRawLayoutParams()
        {
            WindowManager.LayoutParams result = new WindowManager.LayoutParams();
            for (Field field:getClass().getFields())
            {
                try{
                    Field rawField = WindowManager.LayoutParams.class.getField(field.getName());
                    rawField.set(result,field.get(this));
                }catch (NoSuchFieldException | IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }
    }
}
