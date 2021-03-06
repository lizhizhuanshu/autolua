package top.lizhistudio.autolua.rpc;

import java.lang.reflect.Method;
import java.util.HashMap;

import top.lizhistudio.autolua.annotation.RPCMethod;

public class RPCService {
    private final HashMap<String, Method> methods;
    private final Class<?> aClass;
    private final Object o;
    public RPCService(Class<?> aClass,Object o)
    {
        this.aClass = aClass;
        methods = new HashMap<>();
        for (Method method:aClass.getMethods())
        {
            RPCMethod rpcMethod = method.getAnnotation(RPCMethod.class);
            if (rpcMethod != null)
            {
                String name = rpcMethod.alias();
                if (name.equals(""))
                    name = method.getName();
                methods.put(name,method);
            }
        }
        this.o = o;
    }

    public Method getMethod(String name)
    {
        return methods.get(name);
    }

    public Object getService()
    {
        return o;
    }

    public Class<?> getInterface()
    {
        return aClass;
    }
}
