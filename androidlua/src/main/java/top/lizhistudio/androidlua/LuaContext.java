package top.lizhistudio.androidlua;


import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;
import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaInvokeError;
import top.lizhistudio.androidlua.exception.LuaLoadError;
import top.lizhistudio.androidlua.exception.LuaTypeError;

public class LuaContext {
    public static final int LUA_MAXSTACK = 1000000;
    public static final int LUA_REGISTRYINDEX = -1001000;
    public static final int LUA_RIDX_MAINTHREAD = 1;
    public static final int LUA_RIDX_GLOBALS = 2;

    public static final int LUA_TNONE = -1;
    public static final int LUA_TNIL = 0;
    public static final int LUA_TBOOLEAN = 1;
    public static final int LUA_TLIGHTUSERDATA = 2;
    public static final int LUA_TNUMBER = 3;
    public static final int LUA_TSTRING = 4;
    public static final int LUA_TTABLE = 5;
    public static final int LUA_TFUNCTION = 6;
    public static final int LUA_TUSERDATA = 7;
    public static final int LUA_TTHREAD = 8;

    public static final int LUA_MULTRET = -1;
    public static final int LUA_OK = 0;
    public static final int LUA_YIELD = 1;
    public static final int LUA_ERRRUN = 2;
    public static final int LUA_ERRSYNTAX = 3;
    public static final int LUA_ERRMEM = 4;
    public static final int LUA_ERRGCMM = 5;
    public static final int LUA_ERRERR = 6;


    public enum CODE_TYPE{
        TEXT_BINARY(0),TEXT(1),BINARY(2);
        private final int code;
        CODE_TYPE(int code)
        {
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }
    private static final String TAG = "LuaContext";
    private long nativeLua;
    private final LongSparseArray<Object> objectCache;
    private final JavaObjectWrapperFactory javaObjectWrapperFactory;



    static {
        System.loadLibrary("lua");
        System.loadLibrary("luajava");
    }

    static native long newLuaState(LuaContext context);
    static native void closeLuaState(long nativeLua);

    static native long toPointer(long nativeLua,int index);
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
    static native void push(long nativeLua,LuaHandler luaHandler);
    static native void pushJavaObjectMethod(long nativeLua);


    static native int loadBuffer(long nativeLua,byte[] code,String chunkName,int codeType);
    static native int loadFile(long nativeLua, String fileName,int codeType);
    static native int reference(long nativeLua,int tableIndex);
    static native void unReference(long nativeLua,int tableIndex,int reference);

    static native int pCall(long nativeLua, int argNumber, int resultNumber, int errorFunctionIndex);



    static native int type(long nativeLua,int index);
    static native boolean isInteger(long nativeLua,int index);
    static native boolean isJavaObjectWrapper(long nativeLua,int index);
    static native JavaObjectWrapper toJavaObject(long nativeLua, int index);

    static native void setGlobal(long nativeLua,String key);
    static native void setTop(long nativeLua,int index);
    static native void setField(long nativeLua,int tableIndex, String key);
    static native void setI(long nativeLua,int tableIndex,long n);
    static native void setTable(long nativeLua,int tableIndex);

    static native void pop(long nativeLua, int n);

    static native int getTop(long nativeLua);
    static native int getGlobal(long nativeLua,String key);
    static native int getField(long nativeLua,int tableIndex,String key);
    static native boolean getStack(long nativeLua,int level,DebugInfo debugInfo);
    static native boolean getInfo(long nativeLua,String what,DebugInfo debugInfo);



    static native long newDebugInfo();
    static native void releaseDebugInfo(long nativePrint);
    static native String getDebugInfoName(long nativePrint);
    static native String getDebugInfoNameWhat(long nativePrint);
    static native String getDebugInfoSource(long nativePrint);
    static native String getDebugInfoShortSource(long nativePrint);
    static native String getDebugInfoWhat(long nativePrint);
    static native boolean isDebugInfoTailCall(long nativePrint);
    static native boolean isDebugInfoVarArg(long nativePrint);
    static native int getDebugInfoCurrentLine(long nativePrint);
    static native int getDebugInfoLastLineDefined(long nativePrint);
    static native int getDebugInfoLineDefined(long nativePrint);
    static native int getDebugInfoParamsSum(long nativePrint);
    static native int getDebugInfoUpValueSum(long nativePrint);


    public LuaContext(@NonNull JavaObjectWrapperFactory javaObjectWrapperFactory)
    {
        nativeLua = newLuaState(this);
        objectCache = new LongSparseArray<>();
        this.javaObjectWrapperFactory = javaObjectWrapperFactory;
    }


    public long toPointer(int index)
    {
        return toPointer(nativeLua,index);
    }


    public long toInteger(int index) {
        return toInteger(nativeLua,index);
    }


    public double toNumber(int index) {
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

    private void throwTypeError(int index)
    {
        throw new LuaTypeError("lua "+nativeLua+" index "+index+" can't to Java Object");
    }


    public Object toJavaObject(int index) {
        switch (type(index))
        {
            case LUA_TBOOLEAN:
                return toBoolean(index);
            case LUA_TNUMBER:
                if (isInteger(index))
                    return toInteger(index);
                else
                    return toNumber(index);
            case LUA_TSTRING:
                return toString(index);
            case LUA_TNONE:
            case LUA_TNIL:
                return null;
            case LUA_TUSERDATA:
            {
                if (isJavaObjectWrapper(nativeLua,index))
                    return toJavaObject(nativeLua,index).getContent();
            }
        }
        throwTypeError(index);
        return null;
    }



    public Object toJavaObject(int index, Class<?> aClass) {
        if (aClass.equals(Object.class))
        {
            return toJavaObject(index);
        }
        else if (aClass.isPrimitive())
        {
            if (aClass == Integer.TYPE)
                return (int)toInteger(index);
            else if(aClass == Boolean.TYPE)
                return toBoolean(index);
            else if (aClass == Long.TYPE)
                return toInteger(index);
            else if (aClass == Float.TYPE)
                return (float)toNumber(index);
            else if (aClass == Double.TYPE)
                return toNumber(index);
            else if (aClass == Byte.TYPE)
                return (byte)toInteger(index);
            else if (aClass == Short.TYPE)
                return (short)toInteger(index);
        }else if (aClass.isAssignableFrom(Number.class)) {
            if (aClass.isAssignableFrom(Integer.class))
                return (int) toInteger(index);
            else if (aClass.isAssignableFrom(Long.class))
                return toInteger(index);
            else if (aClass.isAssignableFrom(Float.class))
                return (float) toNumber(index);
            else if (aClass.isAssignableFrom(Double.class))
                return toNumber(index);
            else if (aClass.isAssignableFrom(Byte.class))
                return (byte) toInteger(index);
            else if (aClass.isAssignableFrom(Short.class))
                return (short) toInteger(index);
        }else if(aClass.isAssignableFrom(String.class))
            return toString(index);
        else if(aClass.isAssignableFrom(Boolean.class))
            return toBoolean(index);
        else if(aClass.isAssignableFrom(byte[].class) && type(index) == LUA_TSTRING)
            return toBytes(index);
        else if (isJavaObjectWrapper(nativeLua,index))
            return toJavaObject(nativeLua,index).getContent();
        throwTypeError(index);
        return null;
    }


    public Object[] toJavaObjects(int originIndex, Class<?>[] classes) {
        Object[] result = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            result[i] = toJavaObject(originIndex+i,classes[i]);
        }
        return result;
    }


    public Object[] toJavaObjects(int originIndex, int sum) {
        Object[] result = new Object[sum];
        for (int i = 0; i < sum; i++) {
            result[i] = toJavaObject(originIndex+i);
        }
        return result;
    }

    private void pushObject(Class<?> aClass,Object o)
    {
        if (o == null)
        {
            pushNil();
            return;
        }
        if (aClass.equals(Object.class))
            aClass = o.getClass();
        if (aClass.isPrimitive())
        {
            if (aClass == Integer.TYPE)
                push((int)o);
            else if(aClass == Boolean.TYPE)
                push((boolean)o);
            else if (aClass == Long.TYPE)
                push((long)o);
            else if (aClass == Float.TYPE)
                push((float)o);
            else if (aClass == Double.TYPE)
                push((double)o);
            else if (aClass == Byte.TYPE)
                push((byte)o);
            else if (aClass == Short.TYPE)
                push((short)o);
        }else if (Number.class.isAssignableFrom(aClass)) {
            if (Long.class.isAssignableFrom(aClass))
                push((long)o);
            else if (Double.class.isAssignableFrom(aClass))
                push((double)o);
        }else if(Boolean.class.equals(aClass))
            push((boolean)o);
        else if(String.class.isAssignableFrom(aClass))
            push(((String)o).getBytes());
        else
        {
            push(nativeLua, javaObjectWrapperFactory.newObjectWrapper(aClass, o));
        }
    }



    public void push(Class<?> aClass, Object o) {
        pushObject(aClass, o);
    }


    public void push(Class<?> aClass) {
        push(nativeLua, javaObjectWrapperFactory.getClassWrapper(aClass));
    }


    public void push(Object o) {
        if (o == null)
            pushNil();
        else if( o instanceof Class)
            push((Class<?>)o);
        else
            pushObject(o.getClass(),o);
    }


    public void push(LuaHandler luaHandler) {
        push(nativeLua,luaHandler);
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


    public void pushJavaObjectMethod() {
        pushJavaObjectMethod(nativeLua);
    }


    public int loadBuffer(byte[] code, String chunkName, CODE_TYPE mode) {
        return loadBuffer(nativeLua,code,chunkName,mode.getCode());
    }


    public int loadFile(String filePath, CODE_TYPE mode) {
        return loadFile(nativeLua,filePath,mode.getCode());
    }


    public int reference(int tableIndex) {
        return reference(nativeLua,tableIndex);
    }


    public void unReference(int tableIndex, int reference) {
        unReference(nativeLua,tableIndex,reference);
    }


    public int pCall(int argSum, int resultSum, int errorHandlerIndex) {
        return pCall(nativeLua,argSum,resultSum,errorHandlerIndex);
    }


    public void setGlobal(String key) {
        setGlobal(nativeLua,key);
    }


    public int type(int index) {
        return type(nativeLua,index);
    }


    public boolean isInteger(int index) {
        return isInteger(nativeLua,index);
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


    public boolean isJavaObject(int index) {
        return isJavaObjectWrapper(nativeLua,index);
    }


    public boolean getStack(int level, DebugInfo debugInfo) {
        return getStack(nativeLua,level,debugInfo);
    }


    public boolean getInfo(String what, DebugInfo debugInfo) {
        return getInfo(nativeLua,what,debugInfo);
    }


    public int getGlobal(String key) {
        return getGlobal(nativeLua,key);
    }


    public int getField(int tableIndex, String key) {
        return getField(nativeLua,tableIndex,key);
    }


    public void setI(int tableIndex, long n) {
        setI(nativeLua,tableIndex,n);
    }


    public void setTable(int tableIndex) {
        setTable(nativeLua,tableIndex);
    }


    public void setField(int tableIndex, String key) {
        setField(nativeLua,tableIndex,key);
    }


    public long getNativeLua()
    {
        return nativeLua;
    }



    private void checkLoadResult(int result)
    {
        if (result == LUA_OK)
            return;
        String message = toString(-1);
        pop(1);
        throw new LuaLoadError(message);
    }

    private void checkCallResult(int result)
    {
        if (result == LUA_OK)
            return;
        try{
            if (type(-1) == LUA_TSTRING)
                throw new LuaInvokeError(toString(-1));
            if (isJavaObject(-1))
            {
                Object javaObject = toJavaObject(-1);
                if (javaObject instanceof LuaError)
                    throw (LuaError)javaObject;
                else if(javaObject instanceof Throwable)
                    throw new LuaInvokeError((Throwable)javaObject);
                else
                    throw new LuaInvokeError("unknown error");
            }
        }finally {
            pop(1);
        }

    }

    private Object[] callAndGetResult(int oldTop,int errorHandlerIndex)
    {
        int result = pCall(0,LUA_MULTRET,errorHandlerIndex);
        checkCallResult(result);
        int resultSum = getTop()-oldTop;
        try{
            return toJavaObjects(oldTop+1,resultSum);
        }finally {
            setTop(oldTop);
        }
    }

    public Object[] execute(byte[] code,String chunkName,int errorHandlerIndex)
    {
        int oldTop = getTop();
        int result = loadBuffer(code,chunkName,CODE_TYPE.TEXT_BINARY);
        checkLoadResult(result);
        return callAndGetResult(oldTop,errorHandlerIndex);
    }
    public Object[] executeFile(String path,int errorHandlerIndex)
    {
        int oldTop = getTop();
        int result = loadFile(path,CODE_TYPE.TEXT_BINARY);
        checkLoadResult(result);
        return callAndGetResult(oldTop,errorHandlerIndex);
    }
    public Object[] execute(byte[] code,String chunkName,@NonNull LuaHandler errorHandler)
    {
        int oldTop = getTop();
        try{
            push(errorHandler);
            int result = loadBuffer(code,chunkName,CODE_TYPE.TEXT_BINARY);
            checkLoadResult(result);
            return callAndGetResult(oldTop+1,oldTop+1);
        }finally {
            setTop(oldTop);
        }
    }
    public Object[] executeFile(String path,@NonNull LuaHandler errorHandler)
    {
        int oldTop = getTop();
        try{
            push(errorHandler);
            int result = loadFile(path,CODE_TYPE.TEXT_BINARY);
            checkLoadResult(result);
            return callAndGetResult(oldTop+1,oldTop+1);
        }finally {
            setTop(oldTop);
        }
    }
    public Object[] execute(byte[] code)
    {
        return execute(code,"origin",0);
    }
    public Object[] executeFile(String path)
    {
        return executeFile(path,0);
    }
    public Object[] execute(String code)
    {
        return execute(code.getBytes());
    }


    public void setGlobal(String key,Class<?> aClass, Object javaObject)
    {
        push(aClass,javaObject);
        setGlobal(key);
    }

    public void setGlobal(String key,LuaHandler luaHandler)
    {
        push(luaHandler);
        setGlobal(key);
    }

    public void setGlobal(String key,Class<?> aClass)
    {
        push(aClass);
        setGlobal(key);
    }

    public int require(String modeName)
    {
        getGlobal("require");
        push(modeName.getBytes());
        return pCall(1,1,0);
    }

    public <T> void tableToStruct(int tableIndex,@NonNull T object) throws IllegalAccessException {
        Class<?> tClass = object.getClass();
        for (Field field:tClass.getFields())
        {
            String name = field.getName();
            int type = getField(tableIndex,name);
            try{
                if(type != LuaContext.LUA_TNIL)
                {
                    Object o = toJavaObject(-1,field.getType());
                    field.set(object,o);
                }
            }finally {
                pop(1);
            }
        }
    }


    public String coerceToString(int index)
    {
        switch (type(index))
        {
            case LUA_TNONE:
            case LUA_TNIL:return "nil";
            case LUA_TBOOLEAN:return String.valueOf(toBoolean(index));
            case LUA_TFUNCTION:return "function@"+toPointer(index);
            case LUA_TLIGHTUSERDATA:return "lightUserdata@"+toPointer(index);
            case LUA_TNUMBER:{
                if (isInteger(index))
                {
                    return String.valueOf(toInteger(index));
                }
                return String.valueOf(toNumber(index));
            }
            case LUA_TSTRING:return toString(index);
            case LUA_TTABLE:return "table@"+toPointer(index);
            case LUA_TTHREAD:return "thread@"+toPointer(index);
            case LUA_TUSERDATA:{
                if (isJavaObject(index))
                {
                    return toJavaObject(index).toString();
                }else
                    return "userdata@"+toPointer(index);
            }
        }
        return "unknown";
    }


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
