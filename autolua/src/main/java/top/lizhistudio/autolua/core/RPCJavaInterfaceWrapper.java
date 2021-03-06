package top.lizhistudio.autolua.core;

import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.wrapper.JavaClassWrapper;
import top.lizhistudio.autolua.annotation.RPCMethod;

public class RPCJavaInterfaceWrapper extends JavaClassWrapper {
    private final HashMap<String, Method> methodHashMap ;
    public RPCJavaInterfaceWrapper(Class<?> o) {
        super(o);
        methodHashMap = new HashMap<>();
        for (Method method:o.getMethods())
        {
            RPCMethod rpcMethod = method.getAnnotation(RPCMethod.class);
            if (rpcMethod != null)
            {
                String name = rpcMethod.alias();
                if (name.equals(""))
                    name = method.getName();
                methodHashMap.put(name,method);
            }
        }
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
}
