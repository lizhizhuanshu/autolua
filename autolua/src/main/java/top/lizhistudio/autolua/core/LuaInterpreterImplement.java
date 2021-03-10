package top.lizhistudio.autolua.core;

import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.exception.LuaInvokeError;
import top.lizhistudio.autolua.rpc.Callback;

public class LuaInterpreterImplement implements LuaInterpreter {
    private LuaContext context;
    private final AtomicBoolean isRunning;
    private final LuaContextFactory luaContextFactory;
    private final LuaHandler errorHandler;

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

    public LuaInterpreterImplement(LuaContextFactory luaContextFactory,LuaHandler errorHandler)
    {
        this.luaContextFactory = luaContextFactory;
        isRunning = new AtomicBoolean(false);
        this.errorHandler = errorHandler;
    }

    private void checkCallResult(LuaContext context, int r)
    {
        if (r != LuaContext.LUA_OK)
        {
            if (context.type(-1) == LuaContext.LUA_TSTRING)
            {
                String message = context.toString(-1);
                context.pop(1);
                throw new LuaInvokeError(message);
            }else
            {
                context.pop(1);
                throw new LuaInvokeError("unknown error");
            }
        }
    }


    private Object[] executeWrap(byte[] code,String chunkName)
    {
        nowThread = Thread.currentThread();
        if (errorHandler != null)
            return checkLuaContext().execute(code,chunkName,errorHandler);
        return checkLuaContext().execute(code,chunkName,0);
    }

    private Object[] executeFileWrap(String path)
    {
        nowThread = Thread.currentThread();
        if (errorHandler != null)
            return checkLuaContext().executeFile(path,errorHandler);
        return checkLuaContext().executeFile(path,0);
    }


    @Override
    public Object[] execute(byte[] code, String chunkName) {
        if (isRunning.compareAndSet(false,true))
        {
            try{
                return executeWrap(code,chunkName);
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
                return executeFileWrap(path);
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
                        callback.onCompleted(executeWrap(code,chunkName));
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
                        callback.onCompleted(executeFileWrap(path));
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

    private String buildSetLoadPathScript(String projectPath)
    {
        return String.format("package.path = '%s/?.lua;%s/?/init.lua;' .. package.path \n",projectPath,projectPath);
    }

    private String buildSetLoadLibraryPathScript(String projectPath)
    {
        return String.format("package.cpath = '%s/lib?.so;%s/?.so;' .. package.cpath \n",projectPath,projectPath);
    }


    @Override
    public boolean setLoadScriptPath(String path) {
        String script = buildSetLoadPathScript(path);
        try{
            checkLuaContext().execute(script);
            return true;
        }catch (Throwable e)
        {
            return false;
        }
    }

    @Override
    public boolean setLoadLibraryPath(String path) {
        String script = buildSetLoadLibraryPathScript(path);
        try{
            checkLuaContext().execute(script);
            return true;
        }catch (Throwable e)
        {
            return false;
        }
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