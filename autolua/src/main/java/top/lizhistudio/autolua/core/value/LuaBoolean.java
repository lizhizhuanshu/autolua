package top.lizhistudio.autolua.core.value;

import top.lizhistudio.androidlua.LuaContext;

public class LuaBoolean extends LuaValue {
    private static volatile LuaBoolean TRUE;
    private static volatile LuaBoolean FALSE;
    private final boolean v;
    private LuaBoolean(boolean v)
    {
        this.v = v;
    }

    @Override
    public boolean toBoolean() {
        return v;
    }

    @Override
    public LuaContext.VALUE_TYPE type() {
        return LuaContext.VALUE_TYPE.BOOLEAN;
    }

    public static LuaBoolean getTrue() {
        if (TRUE == null) {
            synchronized (LuaBoolean.class) {
                if (TRUE == null) {
                    TRUE = new LuaBoolean(true);
                }
            }
        }
        return TRUE;
    }

    public static LuaBoolean getFalse() {
        if (FALSE == null) {
            synchronized (LuaBoolean.class) {
                if (FALSE == null) {
                    FALSE = new LuaBoolean(false);
                }
            }
        }
        return FALSE;
    }


}
