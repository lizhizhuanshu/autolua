package top.lizhistudio.androidlua.wrapper;


import top.lizhistudio.androidlua.JavaObjectWrapper;
import top.lizhistudio.androidlua.LuaContext;

public abstract class JavaBaseWrapper<T> implements JavaObjectWrapper {
    protected T content;

    public JavaBaseWrapper(T o)
    {
        this.content = o;
    }

    @Override
    public Object getContent()
    {
        return content;
    }

    @Override
    public int __equal(LuaContext context) throws Throwable {
        Object v = context.toJavaObject(2);
        if (content == null)
        {
            context.push(v == null);
        }else
        {
            context.push(content.equals(v));
        }
        return 1;
    }
}
