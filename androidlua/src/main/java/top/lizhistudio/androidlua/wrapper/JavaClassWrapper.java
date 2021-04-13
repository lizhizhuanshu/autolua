package top.lizhistudio.androidlua.wrapper;


import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.annotation.LuaMethod;

public abstract class JavaClassWrapper extends JavaBaseWrapper<Class<?>> {
    public JavaClassWrapper(Class<?> o) {
        super(o);
    }
    public abstract int  __index(LuaContext context, Object o) throws Throwable;
    public abstract int __newIndex(LuaContext context,Object o) throws Throwable;
    public abstract int callMethod(LuaContext context,String methodName,Object o) throws Throwable;

    public static <T>void pushMethod(@NonNull Class<?> aClass, String[] methodNames, HashMap<String,T> methodHashMap)
    {
        for (String name:methodNames)
        {
            methodHashMap.put(name,null);
        }

        for (Method method :
                aClass.getMethods()) {
            String name = method.getName();
            if (methodHashMap.containsKey(name))
            {
                methodHashMap.put(name,(T)method);
            }
        }
    }

    public static <T> void pushMethodByAnnotation(@NonNull Class<?> aClass,HashMap<String,T> methodHashMap)
    {
        for (Method method :
                aClass.getMethods()) {
            LuaMethod luaMethod = method.getAnnotation(LuaMethod.class);
            if (luaMethod != null)
            {
                String name = luaMethod.alias();
                if (name.equals(""))
                    name = method.getName();
                methodHashMap.put(name,(T)method);
            }
        }
    }

    @Override
    public int __index(LuaContext context) throws Throwable
    {
        return __index(context,null);
    }

    @Override
    public int __newIndex(LuaContext context) throws Throwable
    {
        return __newIndex(context,null);
    }

    @Override
    public int callMethod(LuaContext context,String methodName) throws Throwable
    {
        return callMethod(context,methodName,null);
    }

    @Override
    public int __call(LuaContext context) throws Throwable {
        Object r = content.newInstance();
        context.push(content,r);
        return 1;
    }

    @Override
    public int __len(LuaContext context) throws Throwable {
        return 0;
    }
}
