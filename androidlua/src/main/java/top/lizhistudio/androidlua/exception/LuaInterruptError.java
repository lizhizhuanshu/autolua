package top.lizhistudio.androidlua.exception;

import top.lizhistudio.androidlua.LuaJava;

public class LuaInterruptError extends LuaError {
    private static final String MESSAGE = "interrupt";
    public LuaInterruptError()
    {
        super(MESSAGE);
    }
}
