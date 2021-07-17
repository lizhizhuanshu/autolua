package top.lizhistudio.autolua.core.rpc;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import top.lizhistudio.androidlua.exception.LuaError;

public class Util {
    public static void receive(InputStream is,byte[] buffer,int origin,int size) throws IOException
    {
        int completed = 0;
        int nowSize;
        while (completed <size)
        {
            nowSize = is.read(buffer,origin+completed,size-completed);
            if (nowSize <= 0)
                throw new IOException();
            completed+=nowSize;
        }
    }

    private static void throwLuaError(String className,String message)
    {
        try{
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object o = constructor.newInstance(message);
            if (o instanceof LuaError)
                throw (LuaError) o;
            throw new LuaError((Throwable)o);
        }catch (ClassNotFoundException |
                NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException e)

        {
            throw new LuaError(message);
        }
    }

    public static void checkException(Protocol.Message message)
    {
        if (message.getMessageCase() == Protocol.Message.MessageCase.ERROR)
        {
            Protocol.LuaError luaError = message.getError();
            throwLuaError(luaError.getType(),luaError.getMessage());
        }
    }


    public static String readLine(InputStream is)throws IOException
    {
        StringBuilder builder = new StringBuilder();
        int b;
        while ((b = is.read())>=0){
            if (b == '\n')
                return builder.toString();
            builder.append((char)b);
        }
        throw new IOException();
    }
}
