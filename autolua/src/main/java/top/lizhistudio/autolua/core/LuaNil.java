package top.lizhistudio.autolua.core;

import top.lizhistudio.androidlua.LuaContext;

public class LuaNil extends LuaValue{
    private static volatile LuaNil NIL;

    private LuaNil()
    {
    }

    @Override
    public LuaContext.VALUE_TYPE type() {
        return LuaContext.VALUE_TYPE.NIL;
    }

    @Override
    public boolean toBoolean() {
        return false;
    }

    static LuaNil getValue(){
        if (NIL == null) {
            synchronized (LuaBoolean.class) {
                if (NIL == null) {
                    NIL = new LuaNil();
                }
            }
        }
        return NIL;
    }
}
