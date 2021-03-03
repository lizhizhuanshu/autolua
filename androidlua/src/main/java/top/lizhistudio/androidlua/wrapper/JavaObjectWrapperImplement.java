package top.lizhistudio.androidlua.wrapper;

import top.lizhistudio.androidlua.LuaContext;

public class JavaObjectWrapperImplement extends JavaBaseWrapper<Object> {
    private final JavaClassWrapper javaClassWrapper;

    public JavaObjectWrapperImplement(Object o, JavaClassWrapper javaClassWrapper)
    {
        super(o);
        this.javaClassWrapper = javaClassWrapper;
    }

    @Override
    public int __call(LuaContext context) throws Throwable {
        return 0;
    }

    @Override
    public int __index(LuaContext context) throws Throwable {
        return javaClassWrapper.__index(context, content);
    }

    @Override
    public int __newIndex(LuaContext context) throws Throwable {
        return javaClassWrapper.__newIndex(context, content);
    }


    @Override
    public int __len(LuaContext context) throws Throwable {
        return 0;
    }

    @Override
    public int callMethod(LuaContext context, String methodName) throws Throwable {
        return javaClassWrapper.callMethod(context,methodName, content);
    }
}
