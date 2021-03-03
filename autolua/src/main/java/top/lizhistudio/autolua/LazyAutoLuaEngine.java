package top.lizhistudio.autolua;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LazyAutoLuaEngine implements AutoLuaEngine {
    private static final Object[] NULL_RESULT = new Object[0];
    private final LuaInterpreter interpreter;
    private volatile AutoLuaEngine autoLuaEngine = null;
    private static final String message = "正在初始化AutoLuaEngine的运行环境.......";
    private final AtomicBoolean isCreating = new AtomicBoolean(false);

    protected abstract void onHint(String message);
    protected abstract AutoLuaEngine create(LuaInterpreter interpreter);

    public LazyAutoLuaEngine(LuaInterpreter interpreter)
    {
        this.interpreter = interpreter;
    }


    private void onCheckCreate()
    {
        if (isCreating.compareAndSet(false,true))
        {
            if (autoLuaEngine == null)
            {
                new Thread()
                {
                    @Override
                    public void run() {
                        try{
                            autoLuaEngine = create(interpreter);
                        }catch (Throwable e)
                        {
                            onHint(e.getMessage());
                        }finally {
                            isCreating.set(false);
                        }
                    }
                }.start();
            }
        }
        onHint(message);
    }

    @Override
    public void exit() {
        onCheckCreate();
    }

    @Override
    public Object[] execute(byte[] code, String chunkName) {
        onCheckCreate();
        return NULL_RESULT;
    }

    @Override
    public Object[] executeFile(String path) {
        onCheckCreate();
        return NULL_RESULT;
    }

    @Override
    public boolean isRunning() {
        onCheckCreate();
        return false;
    }

    @Override
    public void reset() {
        onCheckCreate();
    }

    @Override
    public void interrupt() {
        onCheckCreate();
    }

    @Override
    public void execute(byte[] code, String chunkName, Callback callback) {
        onCheckCreate();
        callback.onCompleted(NULL_RESULT);
    }

    @Override
    public void executeFile(String path, Callback callback) {
        onCheckCreate();
        callback.onCompleted(NULL_RESULT);
    }


    public AutoLuaEngine proxy()
    {
        ProxyHandler proxyHandler = new ProxyHandler(this);
        return (AutoLuaEngine)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{AutoLuaEngine.class},proxyHandler);
    }

    private static class ProxyHandler implements InvocationHandler
    {
        private volatile AutoLuaEngine autoLuaEngine;

        ProxyHandler(AutoLuaEngine autoLuaEngine)
        {
            this.autoLuaEngine = autoLuaEngine;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            AutoLuaEngine nowAutoLua = autoLuaEngine;
            if (nowAutoLua instanceof LazyAutoLuaEngine)
            {
                AutoLuaEngine luaEngine  = ((LazyAutoLuaEngine)nowAutoLua).autoLuaEngine;
                if (luaEngine != null)
                    autoLuaEngine = luaEngine;
            }
            return method.invoke(autoLuaEngine,args);
        }
    }

}
