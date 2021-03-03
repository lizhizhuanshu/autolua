
package top.lizhistudio.androidlua.exception;

public class LuaInvokeError extends LuaError {
    public LuaInvokeError(String msg) {
        super(msg);
    }
    public LuaInvokeError(Throwable throwable)
    {
        super(throwable);
    }
    public LuaInvokeError(String msg, Throwable t) {
        super(msg, t);
    }
}