package top.lizhistudio.autolua.core;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextFactory;

class LuaContextFactoryImplement implements LuaContextFactory {

    static native void injectAutoLua(long nativeLua);

    @Override
    public LuaContext newLuaContext() {
        LuaContextImplement luaContextImplement = new LuaContextImplement();
        injectAutoLua(luaContextImplement.getNativeLua());
        onInitialize(luaContextImplement);
        return luaContextImplement;
    }

    protected void onInitialize(LuaContext luaContext)
    {

    }
}
