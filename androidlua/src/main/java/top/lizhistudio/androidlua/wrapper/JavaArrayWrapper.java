package top.lizhistudio.androidlua.wrapper;

import java.lang.reflect.Array;

import top.lizhistudio.androidlua.LuaContext;

public class JavaArrayWrapper extends JavaBaseWrapper<Object> {
    private final Class<?> aClass;
    public JavaArrayWrapper(Object o,Class<?> aClass) {
        super(o);
        this.aClass = aClass;
    }

    @Override
    public int __call(LuaContext context) throws Throwable {
        return 0;
    }

    @Override
    public int __index(LuaContext context) throws Throwable {
        int index = (int)context.toInteger(2);
        Object result = Array.get(content,index);
        context.push(aClass.getComponentType(),result);
        return 1;
    }

    @Override
    public int __newIndex(LuaContext context) throws Throwable {
        int index = (int)context.toInteger(2);
        Object v = context.toJavaObject(3,aClass.getComponentType());
        Array.set(content,index,v);
        return 0;
    }

    @Override
    public int __len(LuaContext context) throws Throwable {
        context.push(Array.getLength(content));
        return 1;
    }

    @Override
    public int callMethod(LuaContext context, String methodName) throws Throwable {
        return 0;
    }
}
