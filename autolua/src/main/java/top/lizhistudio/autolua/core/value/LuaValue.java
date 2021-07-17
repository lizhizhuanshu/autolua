package top.lizhistudio.autolua.core.value;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.exception.LuaTypeError;

public abstract class LuaValue {
    public static LuaBoolean TRUE(){
        return LuaBoolean.getTrue();
    }
    public static LuaBoolean FALSE(){
        return LuaBoolean.getFalse();
    }
    public static LuaNil NIL(){
        return LuaNil.getValue();
    }

    public abstract LuaContext.VALUE_TYPE type();
    public boolean isInteger(){
        return false;
    }
    public int toInt(){
        return (int)toLong();
    }
    public long toLong(){
        throw new LuaTypeError("don't to long");
    }
    public double toDouble(){
        throw new LuaTypeError("don't to double");
    }
    public float toFloat(){
        return (float)toDouble();
    }
    public boolean toBoolean() {
        throw new LuaTypeError("don't to boolean");
    }
    public byte[] toBytes(){
        throw new LuaTypeError("don't to bytes");
    }
    public String toString(){
        return type()+"@"+hashCode();
    }


}
