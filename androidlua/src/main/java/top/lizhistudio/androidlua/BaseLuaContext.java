package top.lizhistudio.androidlua;

import androidx.annotation.NonNull;

import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaInvokeError;
import top.lizhistudio.androidlua.exception.LuaLoadError;

public abstract class BaseLuaContext implements LuaContext {

    private void checkLoadResult(int result)
    {
        if (result == LUA_OK)
            return;
        String message = toString(-1);
        pop(1);
        throw new LuaLoadError(message);
    }

    private void checkCallResult(int result)
    {
        if (result == LUA_OK)
            return;
        try{
            if (type(-1) == LUA_TSTRING)
                throw new LuaInvokeError(toString(-1));
            if (isJavaObjectWrapper(-1))
            {
                Object javaObject = toJavaObject(-1);
                if (javaObject instanceof LuaError)
                    throw (LuaError)javaObject;
                else if(javaObject instanceof Throwable)
                    throw new LuaInvokeError((Throwable)javaObject);
                else
                    throw new LuaInvokeError("unknown error");
            }
        }finally {
            pop(1);
        }

    }

    private Object[] callAndGetResult(int oldTop,int errorHandlerIndex)
    {
        int result = pCall(0,LUA_MULTRET,errorHandlerIndex);
        checkCallResult(result);
        int resultSum = getTop()-oldTop;
        try{
            return toJavaObjects(oldTop+1,resultSum);
        }finally {
            setTop(oldTop);
        }
    }

    public Object[] execute(byte[] code,String chunkName,int errorHandlerIndex)
    {
        int oldTop = getTop();
        int result = loadBuffer(code,chunkName,CODE_TYPE.TEXT_BINARY);
        checkLoadResult(result);
        return callAndGetResult(oldTop,errorHandlerIndex);
    }
    public Object[] executeFile(String path,int errorHandlerIndex)
    {
        int oldTop = getTop();
        int result = loadFile(path,CODE_TYPE.TEXT_BINARY);
        checkLoadResult(result);
        return callAndGetResult(oldTop,errorHandlerIndex);
    }
    public Object[] execute(byte[] code,String chunkName,@NonNull LuaHandler errorHandler)
    {
        int oldTop = getTop();
        try{
            push(errorHandler);
            int result = loadBuffer(code,chunkName,CODE_TYPE.TEXT_BINARY);
            checkLoadResult(result);
            return callAndGetResult(oldTop+1,oldTop+1);
        }finally {
            setTop(oldTop);
        }
    }
    public Object[] executeFile(String path,@NonNull LuaHandler errorHandler)
    {
        int oldTop = getTop();
        try{
            push(errorHandler);
            int result = loadFile(path,CODE_TYPE.TEXT_BINARY);
            checkLoadResult(result);
            return callAndGetResult(oldTop+1,oldTop+1);
        }finally {
            setTop(oldTop);
        }
    }
    public Object[] execute(byte[] code)
    {
        return execute(code,"origin",0);
    }
    public Object[] executeFile(String path)
    {
        return executeFile(path,0);
    }
    public Object[] execute(String code)
    {
        return execute(code.getBytes());
    }
}
