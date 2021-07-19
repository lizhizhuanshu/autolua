package top.lizhistudio.androidlua;

public abstract class NotReleaseLuaFunctionAdapter implements LuaFunctionAdapter{
    @Override
    public void onRelease() {
    }
}
