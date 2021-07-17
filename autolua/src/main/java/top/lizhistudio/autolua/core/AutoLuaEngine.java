package top.lizhistudio.autolua.core;


import androidx.annotation.NonNull;

import java.util.ArrayList;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaRuntimeError;
import top.lizhistudio.androidlua.exception.LuaTypeError;
import top.lizhistudio.autolua.core.value.LuaNumber;
import top.lizhistudio.autolua.core.value.LuaString;
import top.lizhistudio.autolua.core.value.LuaValue;

public class AutoLuaEngine implements LuaInterpreter{
    private final LuaContextFactory luaContextFactory;
    private LuaContext nowLuaContext;
    private final Object luaContextMutex = new Object();
    private boolean isRunning = false;
    private Thread workThread = null;
    private final ArrayList<LuaFunctionAdapter> initializeHandlers;

    public AutoLuaEngine(@NonNull LuaContextFactory luaContextFactory)
    {
        this.luaContextFactory = luaContextFactory;
        initializeHandlers = new ArrayList<>();
    }

    private void onInitializeLuaContextByHandler()
    {
        try{
            synchronized (initializeHandlers)
            {
                for (LuaFunctionAdapter functionAdapter:initializeHandlers)
                {
                    nowLuaContext.pop(functionAdapter.onExecute(nowLuaContext));
                }
            }
        }catch (LuaError e)
        {
            throw e;
        }catch (Throwable e)
        {
            throw new LuaRuntimeError(e);
        }
    }

    private LuaContext prepareLuaContext()
    {
        synchronized (luaContextMutex)
        {
            if (isRunning)
                throw  new LuaRuntimeError("AutoLua is running");
            if (nowLuaContext == null)
            {
                nowLuaContext = luaContextFactory.newLuaContext();
                onInitializeLuaContextByHandler();
            }
            isRunning = true;
            workThread = Thread.currentThread();
            return nowLuaContext;
        }
    }


    @Override
    public LuaValue[] execute(byte[] code, String chunkName, LuaContext.CODE_TYPE code_type) {
        try{
            LuaContext luaContext = prepareLuaContext();
            int top = luaContext.getTop();
            luaContext.loadBuffer(code,chunkName,code_type);
            luaContext.pCall(0,LuaContext.MULTI_RESULT,0);
            int newTop = luaContext.getTop();
            try{
                return toLuaValues(luaContext,top+1,newTop-top);
            }finally {
                luaContext.setTop(top);
            }

        }finally {
            synchronized (luaContextMutex)
            {
                isRunning = false;
            }
        }
    }

    private LuaValue[] toLuaValues(LuaContext luaContext,int origin,int size)
    {
        LuaValue[] result = new LuaValue[size];
        for (int i = 0; i < size; i++) {
            result[i] = toLuaValue(luaContext,origin+i);
        }
        return result;
    }

    private LuaValue toLuaValue(LuaContext luaContext,int index)
    {
        switch (LuaContext.VALUE_TYPE.valueOf(luaContext.type(index)))
        {
            case NONE:
            case NIL:
                return LuaValue.NIL();
            case STRING:
                return new LuaString(luaContext.toBytes(index));
            case NUMBER:
            {
                if (luaContext.isInteger(index))
                    return LuaNumber.valueOf(luaContext.toLong(index));
                return LuaNumber.valueOf(luaContext.toDouble(index));
            }
            case BOOLEAN:
                return luaContext.toBoolean(index)?LuaValue.TRUE():LuaValue.FALSE();
        }
        throw new LuaTypeError(String.format("The index '%d' lua value can't to java object LuaValue",index));
    }

    @Override
    public LuaValue[] executeFile(String path, LuaContext.CODE_TYPE code_type) {
        try{
            LuaContext luaContext = prepareLuaContext();
            int top = luaContext.getTop();
            luaContext.loadFile(path,code_type);
            luaContext.pCall(0,LuaContext.MULTI_RESULT,0);
            int newTop = luaContext.getTop();
            try{
                return toLuaValues(luaContext,top+1,newTop-top);
            }finally {
                luaContext.setTop(top);
            }
        }finally {
            synchronized (luaContextMutex)
            {
                isRunning = false;
            }
        }
    }

    @Override
    public void executeFile(String filePath, LuaContext.CODE_TYPE code_type, Callback callback) {
        new Thread()
        {
            @Override
            public void run() {
                try{
                    LuaValue[] result = executeFile(filePath,code_type);
                    callback.onCallback(result);
                }catch (Throwable throwable)
                {
                    callback.onError(throwable);
                }
            }
        }.start();
    }

    @Override
    public void execute(byte[] code, String chunkName, LuaContext.CODE_TYPE code_type, Callback callback) {
        new Thread()
        {
            @Override
            public void run() {
                try{
                    LuaValue[] result = execute(code,chunkName,code_type);
                    callback.onCallback(result);
                }catch (Throwable throwable)
                {
                    callback.onError(throwable);
                }
            }
        }.start();
    }

    @Override
    public boolean isRunning() {
        synchronized (luaContextMutex)
        {
            return isRunning;
        }
    }

    @Override
    public void interrupt() {
        synchronized (luaContextMutex)
        {
            if (isRunning)
            {
                workThread.interrupt();
            }
        }
    }

    @Override
    public void destroyNowLuaContext() {
        synchronized (luaContextMutex)
        {
            if (isRunning)
                throw  new LuaRuntimeError("AutoLua is running");
            if (nowLuaContext != null)
            {
                nowLuaContext.destroy();
                nowLuaContext = null;
            }
        }
    }


    public boolean addInitializeHandler(LuaFunctionAdapter luaFunctionAdapter)
    {
        synchronized (initializeHandlers)
        {
            return initializeHandlers.add(luaFunctionAdapter);
        }
    }

    public boolean removeInitializeHandler(LuaFunctionAdapter luaFunctionAdapter)
    {
        synchronized (initializeHandlers)
        {
            return initializeHandlers.remove(luaFunctionAdapter);
        }
    }
}
