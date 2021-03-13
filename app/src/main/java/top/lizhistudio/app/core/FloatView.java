package top.lizhistudio.app.core.ui;

import top.lizhistudio.autolua.annotation.RPCInterface;
import top.lizhistudio.autolua.annotation.RPCMethod;

@RPCInterface
public interface FloatView {
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
