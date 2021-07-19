package top.lizhistudio.androidlua;

public abstract class NotReleaseLuaObjectAdapter implements LuaObjectAdapter{
    @Override
    public void onRelease() {
    }
}
