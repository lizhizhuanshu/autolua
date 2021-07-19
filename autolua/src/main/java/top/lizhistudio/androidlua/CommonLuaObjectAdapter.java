package top.lizhistudio.androidlua;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



public class CommonLuaObjectAdapter extends NotReleaseLuaObjectAdapter{
    private final Object o;
    public CommonLuaObjectAdapter()
    {
        o = this;
    }

    public CommonLuaObjectAdapter(Object o)
    {
        this.o = o;
    }

    @Override
    public boolean hasMethod(String name) {
        Class<?> clazz = o.getClass();
        try{
            clazz.getMethod(name,LuaContext.class);
            return true;
        }catch (NoSuchMethodException e)
        {
            return false;
        }
    }

    @Override
    public List<String> getAllMethodName() {
        ArrayList<String> methods = new ArrayList<>();
        for (Method method: o.getClass().getMethods())
        {
            if (method.getReturnType() == int.class)
            {
                Class<?>[] classes = method.getParameterTypes();
                if (classes.length == 1 && classes[0] == LuaContext.class)
                {
                    methods.add(method.getName());
                }
            }
        }
        return methods;
    }

    @Override
    public int call(String methodName, LuaContext luaContext) throws Throwable {
        Class<?> clazz = this.getClass();
        Method method = clazz.getMethod(methodName,LuaContext.class);
        Object result = method.invoke(o,luaContext);
        return (int)result;
    }

    @Override
    public Object getJavaObject() {
        return o;
    }
}
