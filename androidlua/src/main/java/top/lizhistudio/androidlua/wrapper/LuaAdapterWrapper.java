package top.lizhistudio.androidlua.wrapper;

import android.util.Log;

import java.lang.reflect.Method;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.annotation.LuaMethod;

public class LuaAdapterWrapper extends JavaClassWrapper{
    public LuaAdapterWrapper(Class<?> o) {
        super(o);
    }

    @Override
    public int __index(LuaContext context, Object o) throws Throwable {
        context.pushJavaObjectMethod();
        return 1;
    }

    @Override
    public int __newIndex(LuaContext context, Object o) throws Throwable {
        return 0;
    }

    @Override
    public int callMethod(LuaContext context, String methodName, Object o) throws Throwable {
        Method method = content.getMethod(methodName,LuaContext.class);
        return (int)method.invoke(o,context);
    }



}

