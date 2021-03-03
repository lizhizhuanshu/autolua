package top.lizhistudio.androidlua;


import top.lizhistudio.androidlua.exception.LuaTypeError;

public class LuaJava {

    private LuaJava(){}

    static {
        System.loadLibrary("luajava");
    }

    static native long newLuaState(LuaContext context);
    static native void closeLuaState(long nativeLua);

    static native long toInteger(long nativeLua,int index) throws LuaTypeError;
    static native double toNumber(long nativeLua,int index)throws LuaTypeError;
    static native byte[] toBytes(long nativeLua,int index)throws LuaTypeError;
    static native String toString(long nativeLua,int index)throws LuaTypeError;
    static native boolean toBoolean(long nativeLua,int index);

    static native void push(long nativeLua,long v);
    static native void push(long nativeLua,double v);
    static native void push(long nativeLua,byte[] v);
    static native void push(long nativeLua, boolean v);
    static native void pushNil(long nativeLua);

    static native void push(long nativeLua, JavaObjectWrapper objectWrapper);
    static native void pushJavaObjectMethod(long nativeLua);

    static native void setGlobal(long nativeLua,String key);

    static native int execute(long nativeLua, byte[] code , String chunkName);
    static native int executeFile(long nativeLua, String path);

    static native int type(long nativeLua,int index);
    static native boolean isInteger(long nativeLua,int index);
    static native boolean isJavaObjectWrapper(long nativeLua,int index);
    static native JavaObjectWrapper toJavaObject(long nativeLua, int index);
    static native int getTop(long nativeLua);
    static native void setTop(long nativeLua,int index);
}
