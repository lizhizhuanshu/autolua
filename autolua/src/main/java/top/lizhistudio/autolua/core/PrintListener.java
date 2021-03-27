package top.lizhistudio.autolua.core;

import top.lizhistudio.autolua.annotation.RPCMethod;

public interface PrintListener
{
    @RPCMethod
    void onPrint(String source,int line,String message);
}