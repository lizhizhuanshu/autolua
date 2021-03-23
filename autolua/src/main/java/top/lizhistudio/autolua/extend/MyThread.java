package top.lizhistudio.autolua.extend;

import top.lizhistudio.androidlua.LuaContext;

public class MyThread {
    public int sleep(LuaContext context) throws InterruptedException
    {
        long time = context.toInteger(2);
        java.lang.Thread.sleep(time);
        return 0;
    }
}
