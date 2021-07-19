package top.lizhistudio.autolua.core;


import android.util.LongSparseArray;


import java.util.ArrayList;
import java.util.Vector;

import top.lizhistudio.androidlua.LuaAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.LuaObjectAdapter;
import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;
import top.lizhistudio.androidlua.exception.LuaTypeError;

public class LuaContextImplement implements LuaContext {
    private static final String TAG = "LuaContext";
    private long nativeLua;
    private final LongSparseArray<LuaAdapter> adapterCache;
    private final ArrayList<Thread> threads = new ArrayList<>();
    static {
        System.loadLibrary("autolua");
    }

    static native void injectAutoLua(long nativeLua,boolean isGlobal);

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
    static native void push(long nativeLua, LuaFunctionAdapter luaFunctionAdapter);


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
    static native void rawSet(long nativeLua,int tableIndex);
    static native int rawGet(long nativeLua,int tableIndex);
    static native void setGlobal(long nativeLua,String key);
    static native int getGlobal(long nativeLua,String key);
    static native void pop(long nativeLua, int n);
    static native void createTable(long nativeLua,int arraySize,int dictionarySize);

    public LuaContextImplement()
    {
        nativeLua = newLuaState(this);
        adapterCache = new LongSparseArray<>();
    }

    public void injectAutoLua(boolean isGlobal)
    {
        injectAutoLua(nativeLua,isGlobal);
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


    public void push(LuaFunctionAdapter luaFunctionAdapter) {
        push(nativeLua, luaFunctionAdapter);
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
        try{
            synchronized (threads)
            {
                threads.add(Thread.currentThread());
            }
            pCall(nativeLua,argSum,resultSum,errorHandlerIndex);
        }finally {
            synchronized (threads)
            {
                threads.remove(threads.size()-1);
            }
        }
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
            adapterCache.clear();
        }
    }

    @Override
    public void interrupt() {
        synchronized (threads)
        {
            for(Thread thread:threads)
            {
                thread.interrupt();
            }
        }
    }

    @Override
    public void createTable(int arraySize, int dictionarySize) {
        createTable(nativeLua,arraySize,dictionarySize);
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

    @Override
    public int getGlobal(String key) {
        return getGlobal(nativeLua,key);
    }

    @Override
    public void setGlobal(String key) {
        setGlobal(nativeLua,key);
    }

    @Override
    public int rawGet(int tableIndex) {
        return rawGet(nativeLua,tableIndex);
    }

    @Override
    public void rawSet(int tableIndex) {
        rawSet(nativeLua,tableIndex);
    }

    public int getTable(int tableIndex)
    {
        return getTable(nativeLua,tableIndex);
    }


    @NativeLuaUseMethod
    public void cacheLuaAdapter(long id, LuaAdapter o) {
        adapterCache.put(id,o);
    }

    @NativeLuaUseMethod
    public LuaAdapter getLuaAdapter(long id) {
        return adapterCache.get(id);
    }

    @NativeLuaUseMethod
    public void releaseLuaAdapter(long id) {
        LuaAdapter luaAdapter = getLuaAdapter(id);
        if (luaAdapter != null)
        {
            adapterCache.remove(id);
            luaAdapter.onRelease();
        }
    }
}
