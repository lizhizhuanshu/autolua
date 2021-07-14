package top.lizhistudio.autolua.core;


import android.util.LongSparseArray;

import androidx.annotation.NonNull;


import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaHandler;
import top.lizhistudio.androidlua.LuaObjectAdapter;
import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;
import top.lizhistudio.androidlua.exception.LuaTypeError;

class LuaContextImplement implements LuaContext {
    private static final String TAG = "LuaContext";
    private long nativeLua;
    private final LongSparseArray<Object> objectCache;


    static native long newLuaState(LuaContextImplement context);
    static native void closeLuaState(long nativeLua);

    static native long toPointer(long nativeLua,int index);
    static native long toInteger(long nativeLua,int index) throws LuaTypeError;
    static native double toNumber(long nativeLua,int index)throws LuaTypeError;
    static native byte[] toBytes(long nativeLua,int index)throws LuaTypeError;
    static native String toString(long nativeLua,int index)throws LuaTypeError;
    static native boolean toBoolean(long nativeLua,int index);
    static native LuaObjectAdapter toLuaObjectAdapter(long nativeLua,int index)throws LuaTypeError;
    static native void push(long nativeLua,long v);
    static native void push(long nativeLua,double v);
    static native void push(long nativeLua,byte[] v);
    static native void push(long nativeLua, boolean v);
    static native void pushNil(long nativeLua);

    static native void push(long nativeLua, LuaObjectAdapter objectWrapper);
    static native void push(long nativeLua, LuaHandler luaHandler);


    static native void loadBuffer(long nativeLua,byte[] code,String chunkName,int codeType);
    static native void loadFile(long nativeLua, String fileName,int codeType);
    static native void pCall(long nativeLua, int argNumber, int resultNumber, int errorFunctionIndex);



    static native int type(long nativeLua,int index);
    static native boolean isInteger(long nativeLua,int index);
    static native boolean isLuaObjectAdapter(long nativeLua,int index);

    static native int getTop(long nativeLua);
    static native void setTop(long nativeLua,int index);
    static native void setTable(long nativeLua,int tableIndex);
    static native int getTable(long nativeLua,int tableIndex);
    static native void pop(long nativeLua, int n);





    public LuaContextImplement()
    {
        nativeLua = newLuaState(this);
        objectCache = new LongSparseArray<>();
    }


    public long toPointer(int index)
    {
        return toPointer(nativeLua,index);
    }

    @Override
    public long toLong(int index) {
        return toInteger(nativeLua,index);
    }

    @Override
    public double toDouble(int index) {
        return toNumber(nativeLua,index);
    }

    public String toString(int index) {
        return toString(nativeLua,index);
    }


    public byte[] toBytes(int index) {
        return toBytes(nativeLua,index);
    }


    public boolean toBoolean(int index) {
        return toBoolean(nativeLua,index);
    }

    @Override
    public LuaObjectAdapter toLuaObjectAdapter(int index) {
        return toLuaObjectAdapter(nativeLua,index);
    }

    private void throwTypeError(int index)
    {
        throw new LuaTypeError("lua "+nativeLua+" index "+index+" can't to Java Object");
    }

    public void push(LuaHandler luaHandler) {
        push(nativeLua,luaHandler);
    }

    @Override
    public void push(LuaObjectAdapter v) {
        push(nativeLua,v);
    }


    public void push(long v) {
        push(nativeLua,v);
    }


    public void push(double v) {
        push(nativeLua,v);
    }


    public void push(boolean v) {
        push(nativeLua,v);
    }


    public void push(byte[] v) {
        push(nativeLua,v);
    }


    public void push(String v) {
        push(nativeLua,v.getBytes());
    }


    public void pushNil() {
        pushNil(nativeLua);
    }


    public void loadBuffer(byte[] code, String chunkName, CODE_TYPE mode) {
        loadBuffer(nativeLua,code,chunkName,mode.getCode());
    }


    public void loadFile(String filePath, CODE_TYPE mode) {
        loadFile(nativeLua,filePath,mode.getCode());
    }



    public void pCall(int argSum, int resultSum, int errorHandlerIndex) {
        pCall(nativeLua,argSum,resultSum,errorHandlerIndex);
    }


    public int type(int index) {
        return type(nativeLua,index);
    }


    public boolean isInteger(int index) {
        return isInteger(nativeLua,index);
    }

    @Override
    public boolean isLuaObjectAdapter(int index) {
        return isLuaObjectAdapter(nativeLua,index);
    }


    public synchronized void destroy() {
        if (nativeLua>0)
        {
            closeLuaState(nativeLua);
            nativeLua = 0;
        }
    }


    public void pop(int n) {
        pop(nativeLua,n);
    }


    protected void finalize() throws Throwable {
        destroy();
    }


    public int getTop() {
        return getTop(nativeLua);
    }


    public void setTop(int index) {
        setTop(nativeLua,index);
    }


    public void setTable(int tableIndex) {
        setTable(nativeLua,tableIndex);
    }

    public int getTable(int tableIndex)
    {
        return getTable(nativeLua,tableIndex);
    }

    public long getNativeLua()
    {
        return nativeLua;
    }


//    public String coerceToString(int index)
//    {
//        switch (type(index))
//        {
//            case LUA_TNONE:
//            case LUA_TNIL:return "nil";
//            case LUA_TBOOLEAN:return String.valueOf(toBoolean(index));
//            case LUA_TFUNCTION:return "function@"+toPointer(index);
//            case LUA_TLIGHTUSERDATA:return "lightUserdata@"+toPointer(index);
//            case LUA_TNUMBER:{
//                if (isInteger(index))
//                {
//                    return String.valueOf(toInteger(index));
//                }
//                return String.valueOf(toNumber(index));
//            }
//            case LUA_TSTRING:return toString(index);
//            case LUA_TTABLE:return "table@"+toPointer(index);
//            case LUA_TTHREAD:return "thread@"+toPointer(index);
//            case LUA_TUSERDATA:{
//                if (isJavaObject(index))
//                {
//                    return toJavaObject(index).toString();
//                }else
//                    return "userdata@"+toPointer(index);
//            }
//        }
//        return "unknown";
//    }


    @NativeLuaUseMethod
    public void cacheJavaObject(long id, Object o) {
        objectCache.put(id,o);
    }

    @NativeLuaUseMethod
    public Object getJavaObject(long id) {
        return objectCache.get(id);
    }

    @NativeLuaUseMethod
    public void removeJavaObject(long id) {
        objectCache.remove(id);
    }
}
