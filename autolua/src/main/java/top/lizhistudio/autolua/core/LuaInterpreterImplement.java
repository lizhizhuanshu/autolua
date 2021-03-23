package top.lizhistudio.autolua.core;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.androidlua.DebugInfo;
import top.lizhistudio.androidlua.JavaObjectWrapperFactory;
import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.exception.LuaInvokeError;
import top.lizhistudio.autolua.core.wrapper.UserInterfaceWrapper;
import top.lizhistudio.autolua.extend.Controller;
import top.lizhistudio.autolua.extend.MyThread;
import top.lizhistudio.autolua.extend.Screen;
import top.lizhistudio.autolua.rpc.Callback;
import top.lizhistudio.autolua.rpc.ClientHandler;

public class LuaInterpreterImplement implements LuaInterpreter {
    private LuaContext context;
    private final AtomicBoolean isRunning;
    private final LuaHandler errorHandler;
    private final LuaHandler printHandler;
    private final UserInterfaceWrapper userInterfaceWrapper;
    private volatile Thread nowThread;

    private LuaContext newLuaContext()
    {
        JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
        builder.registerThrowable()
                .registerStruct(PixelFormat.class)
                .registerStruct(WindowManager.LayoutParams.class)
                .registerStruct(Gravity.class)
                .registerLuaAdapter(UserInterfaceWrapper.class)
                .registerInterface(UserInterface.FloatView.class)
                .registerLuaAdapter(MyThread.class)
                .registerLuaAdapter(Controller.class);
        JavaObjectWrapperFactory javaObjectWrapperFactory = builder.build();
        LuaContextImplement luaContext = new LuaContextImplement(javaObjectWrapperFactory);
        luaContext.setGlobal(UserInterface.LUA_CLASS_NAME,UserInterfaceWrapper.class,userInterfaceWrapper);
        luaContext.setGlobal("PixelFormat", PixelFormat.class);
        luaContext.setGlobal("LayoutParamsFlags", WindowManager.LayoutParams.class);
        luaContext.setGlobal("Gravity", Gravity.class);
        luaContext.setGlobal("Controller",Controller.class,Controller.getDefault());
        luaContext.setGlobal("Thread", MyThread.class,new MyThread());
        luaContext.setGlobal("print",printHandler);
        Screen.injectModel(luaContext.getNativeLua());
        return luaContext;
    }

    private LuaContext checkLuaContext()
    {
        synchronized (isRunning)
        {
            if (context == null)
                context = newLuaContext();
            return context;
        }
    }

    public LuaInterpreterImplement(UserInterface userInterface,
                                   PrintListener printListener)
    {
        isRunning = new AtomicBoolean(false);
        this.userInterfaceWrapper = new UserInterfaceWrapper();
        userInterfaceWrapper.setUI(userInterface);
        this.printHandler = new PrintHandler(printListener);
        this.errorHandler = new ErrorHandler(printListener);
    }

    private Object[] executeWrap(byte[] code,String chunkName)
    {
        nowThread = Thread.currentThread();
        return checkLuaContext().execute(code,chunkName,errorHandler);
    }

    private Object[] executeFileWrap(String path)
    {
        nowThread = Thread.currentThread();
        return checkLuaContext().executeFile(path,errorHandler);
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
            context = newLuaContext();
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


    private static class ErrorHandler implements LuaHandler
    {
        private DebugInfo debugInfo = new DebugInfo();
        private PrintListener printListener;
        ErrorHandler(PrintListener printListener)
        {
            this.printListener = printListener;
        }
        @Override
        public int onExecute(LuaContext context) throws Throwable {
            context.getStack(1,debugInfo);
            context.getInfo("Sl",debugInfo);
            String path = debugInfo.getSource();
            int currentLine = debugInfo.getCurrentLine();
            String message = context.coerceToString(-1);
            printListener.onErrorPrint(path,currentLine,message);
            return 1;
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
            ClientHandler clientHandler = Server.getInstance().getClientHandler();
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