package top.lizhistudio.androidlua.wrapper;


import top.lizhistudio.androidlua.LuaContext;

public abstract class JavaClassWrapper extends JavaBaseWrapper<Class<?>> {
    public JavaClassWrapper(Class<?> o) {
        super(o);
    }
    public abstract int  __index(LuaContext context, Object o) throws Throwable;
    public abstract int __newIndex(LuaContext context,Object o) throws Throwable;
    public abstract int callMethod(LuaContext context,String methodName,Object o) throws Throwable;

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
        return 0;
    }

    @Override
    public int __len(LuaContext context) throws Throwable {
        return 0;
    }
}
