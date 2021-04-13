package top.lizhistudio.androidlua.exception;


public class LuaInterruptError extends LuaError {
    private static final String MESSAGE = "interrupt";
    public LuaInterruptError()
    {
        super(MESSAGE);
    }
}
