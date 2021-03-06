package top.lizhistudio.autolua;

import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.exception.LuaInvokeError;
import top.lizhistudio.autolua.rpc.Callback;

public class LuaInterpreterImplement implements LuaInterpreter {
    private LuaContext context;
    private final AtomicBoolean isRunning;
    private final LuaContextFactory luaContextFactory;

    private volatile Thread nowThread;


    private LuaContext checkLuaContext()
    {
        synchronized (isRunning)
        {
            if (context == null)
                context = luaContextFactory.newInstance();
            return context;
        }
    }

    public LuaInterpreterImplement(LuaContextFactory luaContextFactory)
    {
        this.luaContextFactory = luaContextFactory;
        isRunning = new AtomicBoolean(false);
    }

    @Override
    public Object[] execute(byte[] code, String chunkName) {
        if (isRunning.compareAndSet(false,true))
        {
            try{
                nowThread = Thread.currentThread();
                return checkLuaContext().execute(code,chunkName);
            }finally {
                isRunning.set(false);
            }

        }else
            throw new LuaInvokeError("lua is running");
    }

    @Override
    public Object[] executeFile(String path) {
        if (isRunning.compareAndSet(false,true))
        {
            try{
                nowThread = Thread.currentThread();
                return checkLuaContext().executeFile(path);
            }finally {
                isRunning.set(false);
            }
        }else
            throw new LuaInvokeError("lua is running");
    }

    @Override
    public void execute(byte[] code, String chunkName, Callback callback) {
        if (isRunning.compareAndSet(false,true))
        {
            new Thread(){
                @Override
                public void run() {
                    try{
                        nowThread = Thread.currentThread();
                        callback.onCompleted(checkLuaContext().execute(code,chunkName));
                    }catch (Throwable throwable)
                    {
                        callback.onError(throwable);
                    }
                    finally {
                        isRunning.set(false);
                    }
                }
            }.start();
        }else
            callback.onError(new LuaInvokeError("lua is running"));
    }

    @Override
    public void executeFile(String path, Callback callback) {
        if (isRunning.compareAndSet(false,true))
        {
            new Thread(){
                @Override
                public void run() {
                    try{
                        nowThread = Thread.currentThread();
                        callback.onCompleted(checkLuaContext().executeFile(path));
                    }catch (Throwable throwable)
                    {
                        callback.onError(throwable);
                    }
                    finally {
                        isRunning.set(false);
                    }
                }
            }.start();
        }else
            callback.onError(new LuaInvokeError("lua is running"));
    }


    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void reset() {
        if (isRunning.compareAndSet(false,true))
        {
            if (context != null)
                context.destroy();
            context = luaContextFactory.newInstance();
            isRunning.set(false);
        }else
            throw new LuaInvokeError("lua is running");
    }

    @Override
    public void interrupt() {
        if (isRunning.get())
        {
            Thread thread = nowThread;
            if (thread != null)
                thread.interrupt();
        }
    }

}