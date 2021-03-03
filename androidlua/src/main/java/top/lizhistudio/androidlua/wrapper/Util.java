package top.lizhistudio.androidlua.wrapper;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.androidlua.annotation.LuaMethod;

class Util {
    public static <T>void pushMethod(@NonNull Class<?> aClass,String[] methodNames,HashMap<String,T> methodHashMap)
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
}
