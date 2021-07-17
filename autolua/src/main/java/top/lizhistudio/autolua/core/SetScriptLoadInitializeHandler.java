package top.lizhistudio.autolua.core;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;

public class SetScriptLoadInitializeHandler implements LuaFunctionAdapter {
    private volatile String scriptLoadPath = null;
    @Override
    public int onExecute(LuaContext luaContext) throws Throwable {
        String path = scriptLoadPath;
        if (path != null)
        {
            luaContext.getGlobal("package");
            luaContext.push("path");
            luaContext.push(scriptLoadPath);
            luaContext.setTable(-3);
            luaContext.pop(1);
        }
        return 0;
    }

    public void setScriptLoadPath(String path)
    {
        this.scriptLoadPath = path;
    }
}
