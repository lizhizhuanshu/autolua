package top.lizhistudio.app.core;

import android.graphics.PixelFormat;
import android.view.WindowManager;

import java.io.Serializable;
import java.lang.reflect.Field;

import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;

@RPCInterface
public interface UI {
    String SERVICE_NAME = "UI";
    @RPCMethod
    FloatView newFloatView(String name, String uri,LayoutParams layoutParams);
    @RPCMethod
    Object takeSignal() throws InterruptedException;
    @RPCMethod
    FloatView getFloatView(String name);
    @RPCMethod
    void showMessage(String message,int time);

    void putSignal(Object message) throws InterruptedException;

    class LayoutParams implements Serializable
    {
        public int x;
        public int y;
        public int width;
        public int height;
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
