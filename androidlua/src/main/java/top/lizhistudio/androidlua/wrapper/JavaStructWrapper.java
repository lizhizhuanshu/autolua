package top.lizhistudio.androidlua.wrapper;

import java.lang.reflect.Field;

import top.lizhistudio.androidlua.LuaContext;

public class JavaStructWrapper extends JavaClassWrapper {

    public JavaStructWrapper(Class<?> o) {
        super(o);
    }

    @Override
    public int __index(LuaContext context, Object o) throws Throwable {
        Field field = content.getField(context.toString(2));
        Object result = field.get(o);
        context.push(field.getType(),result);
        return 1;
    }

    @Override
    public int __newIndex(LuaContext context, Object o) throws Throwable {
        Field field = content.getField(context.toString(2));
        field.set(o,context.toJavaObject(3,field.getType()));
        return 0;
    }

    @Override
    public int callMethod(LuaContext context, String methodName, Object o) throws Throwable {
        return 0;
    }

}
