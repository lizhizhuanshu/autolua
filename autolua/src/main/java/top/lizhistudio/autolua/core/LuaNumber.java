package top.lizhistudio.autolua.core;

import top.lizhistudio.androidlua.LuaContext;

public class LuaNumber extends LuaValue{
    private LuaNumber(){}

    @Override
    public LuaContext.VALUE_TYPE type() {
        return LuaContext.VALUE_TYPE.NUMBER;
    }

    @Override
    public boolean toBoolean() {
        return true;
    }

    public static LuaNumber valueOf(long value){
        return new LuaLong(value);
    }

    public static LuaNumber valueOf(double value){
        return new LuaDouble(value);
    }

    private static class LuaDouble extends LuaNumber {
        private final double value;
        public LuaDouble(double value){
            this.value = value;
        }

        @Override
        public double toDouble() {
            return value;
        }

        public long toLong(){
            return (long)value;
        }
    }

    private static class LuaLong extends LuaNumber {
        private final long value;
        public LuaLong(long value){
            this.value = value;
        }

        @Override
        public double toDouble() {
            return value;
        }

        public long toLong(){
            return value;
        }

        @Override
        public boolean isInteger() {
            return true;
        }
    }
}
