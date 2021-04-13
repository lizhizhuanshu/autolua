package top.lizhistudio.autolua.core;

import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaHandler;
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
        isRunning = new AtomicBoolean(false);
        this.luaContextFactory = luaContextFactory;
    }

    private Object[] executeWrap(byte[] code,String chunkName)
    {
        nowThread = Thread.currentThread();
        return checkLuaContext().execute(code,chunkName,0);
    }

    private Object[] executeFileWrap(String path)
    {
        nowThread = Thread.currentThread();
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
            context = null;
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

    private static class PrintHandler implements  LuaHandler
    {
        private DebugInfo debugInfo = new DebugInfo();
        private PrintListener printListener;
        PrintHandler(PrintListener printListener)
        {
            this.printListener = printListener;
        }

        private String getMessage(LuaContext context)
        {
            StringBuilder builder = new StringBuilder();
            for (int i=1;i<=context.getTop();i++)
            {
                builder.append(context.coerceToString(i));
                if (i!=context.getTop())
                {
                    builder.append("    ");
                }
            }
            return builder.toString();
        }

        @Override
        public int onExecute(LuaContext luaContext) throws Throwable {
            String message = getMessage(luaContext);
            luaContext.getStack(1,debugInfo);
            luaContext.getInfo("Sl",debugInfo);
            String path = debugInfo.getSource();
            int currentLine = debugInfo.getCurrentLine();
            printListener.onPrint(path,currentLine,message);
            return 0;
        }
    }


}