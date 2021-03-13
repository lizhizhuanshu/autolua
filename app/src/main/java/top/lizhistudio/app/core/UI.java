package top.lizhistudio.app.core.ui;

import android.view.WindowManager;

import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;

@RPCInterface
public interface UI {
    @RPCMethod
    FloatView newFloatView(String name,String uri, WindowManager.LayoutParams layoutParams);
    @RPCMethod
    Object takeMessage() throws InterruptedException;
    @RPCMethod
    FloatView getFloatView(String name);

    void putMessage(Object message) throws InterruptedException;
}
