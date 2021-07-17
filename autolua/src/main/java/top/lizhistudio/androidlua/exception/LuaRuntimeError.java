package top.lizhistudio.androidlua.exception;

public class LuaRuntimeError extends LuaError{
    public LuaRuntimeError(String message)
    {
        super(message);
    }
    public LuaRuntimeError(Throwable throwable)
    {
        super(throwable);
    }
}
