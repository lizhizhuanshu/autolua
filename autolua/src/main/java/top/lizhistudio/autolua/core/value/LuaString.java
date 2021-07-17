package top.lizhistudio.autolua.core.value;

import java.util.Arrays;

import top.lizhistudio.androidlua.LuaContext;

public class LuaString extends LuaValue {
    private final byte[] bytes;
    public LuaString(byte[] bytes)
    {
        this.bytes = Arrays.copyOf(bytes,bytes.length);
    }

    public LuaString(String s){
        this(s.getBytes());
    }

    @Override
    public LuaContext.VALUE_TYPE type() {
        return LuaContext.VALUE_TYPE.STRING;
    }

    @Override
    public byte[] toBytes() {
        return bytes;
    }

    public String toString()
    {
        return new String(bytes);
    }
}
