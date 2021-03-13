package top.lizhistudio.app.core;

import android.view.WindowManager;

import java.io.Serializable;

import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;

@RPCInterface
public interface UI {
    String SERVICE_NAME = "UI";
    //之所以有此方法是因为WindowManager.LayoutParams 无法直接写入流
    @RPCMethod(alias = "newFloatViewByBuffer")
    FloatView newFloatView(String name, String uri,byte[] layoutParams);
    @RPCMethod
    FloatView newFloatView(String name, String uri,WindowManager.LayoutParams layoutParams);
    @RPCMethod
    Object takeSignal() throws InterruptedException;
    @RPCMethod
    FloatView getFloatView(String name);
    @RPCMethod
    void showMessage(String message,int time);

    void putSignal(Object message) throws InterruptedException;
}
