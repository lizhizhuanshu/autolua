package top.lizhistudio.androidlua;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

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
            if (isJavaObject(-1))
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


    public void setGlobal(String key,Class<?> aClass, Object javaObject)
    {
        push(aClass,javaObject);
        setGlobal(key);
    }

    public void setGlobal(String key,LuaHandler luaHandler)
    {
        push(luaHandler);
        setGlobal(key);
    }

    public void setGlobal(String key,Class<?> aClass)
    {
        push(aClass);
        setGlobal(key);
    }

    public int require(String modeName)
    {
        getGlobal("require");
        push(modeName.getBytes());
        return pCall(1,1,0);
    }

    public <T> void tableToStruct(int tableIndex,@NonNull T object) throws IllegalAccessException {
        Class<?> tClass = object.getClass();
        for (Field field:tClass.getFields())
        {
            String name = field.getName();
            int type = getField(tableIndex,name);
            try{
                if(type != LuaContext.LUA_TNIL)
                {
                    Object o = toJavaObject(-1,field.getType());
                    field.set(object,o);
                }
            }finally {
                pop(1);
            }
        }
    }


    public String coerceToString(int index)
    {
        switch (type(index))
        {
            case LUA_TNONE:
            case LUA_TNIL:return "nil";
            case LUA_TBOOLEAN:return String.valueOf(toBoolean(index));
            case LUA_TFUNCTION:return "function@"+toPointer(index);
            case LUA_TLIGHTUSERDATA:return "lightUserdata@"+toPointer(index);
            case LUA_TNUMBER:{
                if (isInteger(index))
                {
                    return String.valueOf(toInteger(index));
                }
                return String.valueOf(toNumber(index));
            }
            case LUA_TSTRING:return toString(index);
            case LUA_TTABLE:return "table@"+toPointer(index);
            case LUA_TTHREAD:return "thread@"+toPointer(index);
            case LUA_TUSERDATA:{
                if (isJavaObject(index))
                {
                    return toJavaObject(index).toString();
                }else
                    return "userdata@"+toPointer(index);
            }
        }
        return "unknown";
    }
}
