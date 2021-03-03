package top.lizhistudio.androidlua.exception;

import java.io.Serializable;

public class LuaError extends RuntimeException implements Serializable {

    public LuaError()
    {
        super();
    }

    public LuaError(String message)
    {
        super(message);
    }

    public LuaError(Throwable throwable)
    {
        super(throwable);
    }

    public LuaError(String message, Throwable throwable)
    {
        super(message,throwable);
    }

}
