package top.lizhistudio.androidlua.wrapper;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.androidlua.LuaContext;


public class JavaInterfaceWrapper extends JavaClassWrapper {
    private final HashMap<String,Method> methodHashMap ;

    private JavaInterfaceWrapper(Class<?> o ,HashMap<String,Method> methodHashMap) {
        super(o);
        this.methodHashMap = methodHashMap;
    }

    public static JavaInterfaceWrapper newInstance(@NonNull Class<?> aClass)
    {
        HashMap<String,Method> methodHashMap = new HashMap<>();
        for (Method method :
                aClass.getMethods()) {
            methodHashMap.put(method.getName(),method);
        }
        return new JavaInterfaceWrapper(aClass,methodHashMap);
    }




    public static JavaInterfaceWrapper newInstance(@NonNull Class<?> aClass, String[] methodNames)
    {
        HashMap<String,Method> methodHashMap = new HashMap<>();
        Util.pushMethod(aClass,methodNames,methodHashMap);
        return new JavaInterfaceWrapper(aClass,methodHashMap);
    }


    public static JavaInterfaceWrapper newInstanceByAnnotation(@NonNull Class<?> aClass)
    {
        HashMap<String,Method> methodHashMap = new HashMap<>();
        Util.pushMethodByAnnotation(aClass,methodHashMap);
        return new JavaInterfaceWrapper(aClass,methodHashMap);
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
        Method method = methodHashMap.get(methodName);
        Object result;
        Class<?>[] classes = method.getParameterTypes();
        if (classes.length == 0)
            result = method.invoke(o);
        else if(classes.length == 1)
            result = method.invoke(o,
                    context.toJavaObject(2,classes[0]));
        else if(classes.length == 2)
            result = method.invoke(o,
                    context.toJavaObject(2,classes[0]),
                    context.toJavaObject(3,classes[1]));
        else
            result = method.invoke(o,context.toJavaObjects(2,classes));
        context.push(method.getReturnType(),result);
        return 1;
    }

    public static class Builder
    {
        private Class<?> aClass;
        private HashMap<String,Method> methodHashMap;
        public Builder reset(Class<?> aClass)
        {
            this.aClass = aClass;
            this.methodHashMap = new HashMap<>();
            return this;
        }

        public Builder(Class<?> aClass)
        {
            reset(aClass);
        }

        public Builder addMethod(String name, @NonNull String alias, Class<?> ... classes) throws NoSuchMethodException
        {
            Method method= aClass.getMethod(name,classes);
            methodHashMap.put(alias,method);
            return this;
        }

        public Builder addMethod(String name, Class<?> ... classes) throws NoSuchMethodException
        {
            Method method= aClass.getMethod(name,classes);
            methodHashMap.put(method.getName(),method);
            return this;
        }

        public JavaInterfaceWrapper build()
        {
            return new JavaInterfaceWrapper(aClass,methodHashMap);
        }

    }

}
