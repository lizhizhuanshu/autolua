package top.lizhistudio.androidlua;

public interface JavaObjectWrapper {
    int __call(LuaContext context)throws Throwable;
    int __index(LuaContext context)throws Throwable;
    int __newIndex(LuaContext context)throws Throwable;
    int __equal(LuaContext context)throws Throwable;
    int __len(LuaContext context)throws Throwable;
    int callMethod(LuaContext context, String methodName) throws Throwable;
    Object getContent();
}
