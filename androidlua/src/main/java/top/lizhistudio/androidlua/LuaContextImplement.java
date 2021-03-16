package top.lizhistudio.androidlua;

import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;
import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.androidlua.exception.LuaInvokeError;
import top.lizhistudio.androidlua.exception.LuaTypeError;

public class LuaContextImplement extends BaseLuaContext {
    private static final String TAG = "LuaContext";
    private long nativeLua;
    private final LongSparseArray<Object> objectCache;
    private final JavaObjectWrapperFactory javaObjectWrapperFactory;

    public LuaContextImplement(@NonNull JavaObjectWrapperFactory javaObjectWrapperFactory)
    {
        nativeLua = LuaJava.newLuaState(this);
        objectCache = new LongSparseArray<>();
        this.javaObjectWrapperFactory = javaObjectWrapperFactory;
    }

    @Override
    public long toPointer(int index)
    {
        return LuaJava.toPointer(nativeLua,index);
    }

    @Override
    public long toInteger(int index) {
        return LuaJava.toInteger(nativeLua,index);
    }

    @Override
    public double toNumber(int index) {
        return LuaJava.toNumber(nativeLua,index);
    }

    @Override
    public String toString(int index) {
        return LuaJava.toString(nativeLua,index);
    }

    @Override
    public byte[] toBytes(int index) {
        return LuaJava.toBytes(nativeLua,index);
    }

    @Override
    public boolean toBoolean(int index) {
        return LuaJava.toBoolean(nativeLua,index);
    }

    private void throwTypeError(int index)
    {
        throw new LuaTypeError("lua "+nativeLua+" index "+index+" can't to Java Object");
    }

    @Override
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
                if (LuaJava.isJavaObjectWrapper(nativeLua,index))
                    return LuaJava.toJavaObject(nativeLua,index).getContent();
            }
        }
        throwTypeError(index);
        return null;
    }


    @Override
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
        else if (LuaJava.isJavaObjectWrapper(nativeLua,index))
            return LuaJava.toJavaObject(nativeLua,index).getContent();
        throwTypeError(index);
        return null;
    }

    @Override
    public Object[] toJavaObjects(int originIndex, Class<?>[] classes) {
        Object[] result = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            result[i] = toJavaObject(originIndex+i,classes[i]);
        }
        return result;
    }

    @Override
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
            pushNil();
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
            LuaJava.push(nativeLua, javaObjectWrapperFactory.newObjectWrapper(aClass, o));
        }
    }


    @Override
    public void push(Class<?> aClass, Object o) {
        pushObject(aClass, o);
    }

    @Override
    public void push(Class<?> aClass) {
        LuaJava.push(nativeLua, javaObjectWrapperFactory.getClassWrapper(aClass));
    }

    @Override
    public void push(Object o) {
        if (o == null)
            pushNil();
        else if( o instanceof Class)
            push((Class<?>)o);
        else
            pushObject(o.getClass(),o);
    }

    @Override
    public void push(LuaHandler luaHandler) {
        LuaJava.push(nativeLua,luaHandler);
    }

    @Override
    public void push(long v) {
        LuaJava.push(nativeLua,v);
    }

    @Override
    public void push(double v) {
        LuaJava.push(nativeLua,v);
    }

    @Override
    public void push(boolean v) {
        LuaJava.push(nativeLua,v);
    }

    @Override
    public void push(byte[] v) {
        LuaJava.push(nativeLua,v);
    }

    @Override
    public void push(String v) {
        LuaJava.push(nativeLua,v.getBytes());
    }

    @Override
    public void pushNil() {
        LuaJava.pushNil(nativeLua);
    }

    @Override
    public void pushJavaObjectMethod() {
        LuaJava.pushJavaObjectMethod(nativeLua);
    }

    @Override
    public int loadBuffer(byte[] code, String chunkName, CODE_TYPE mode) {
        return LuaJava.loadBuffer(nativeLua,code,chunkName,mode.getCode());
    }

    @Override
    public int loadFile(String filePath, CODE_TYPE mode) {
        return LuaJava.loadFile(nativeLua,filePath,mode.getCode());
    }

    @Override
    public int reference(int tableIndex) {
        return LuaJava.reference(nativeLua,tableIndex);
    }

    @Override
    public void unReference(int tableIndex, int reference) {
        LuaJava.unReference(nativeLua,tableIndex,reference);
    }

    @Override
    public int pCall(int argSum, int resultSum, int errorHandlerIndex) {
        return LuaJava.pCall(nativeLua,argSum,resultSum,errorHandlerIndex);
    }

    @Override
    public void setGlobal(String key) {
        LuaJava.setGlobal(nativeLua,key);
    }

    @Override
    public int type(int index) {
        return LuaJava.type(nativeLua,index);
    }

    @Override
    public boolean isInteger(int index) {
        return LuaJava.isInteger(nativeLua,index);
    }


    @Override
    public synchronized void destroy() {
        if (nativeLua>0)
        {
            LuaJava.closeLuaState(nativeLua);
            nativeLua = 0;
        }
    }

    @Override
    public void pop(int n) {
        LuaJava.pop(nativeLua,n);
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
    }

    @Override
    public int getTop() {
        return LuaJava.getTop(nativeLua);
    }

    @Override
    public void setTop(int index) {
        LuaJava.setTop(nativeLua,index);
    }

    @Override
    public boolean isJavaObject(int index) {
        return LuaJava.isJavaObjectWrapper(nativeLua,index);
    }

    @Override
    public boolean getStack(int level, DebugInfo debugInfo) {
        return LuaJava.getStack(nativeLua,level,debugInfo);
    }

    @Override
    public boolean getInfo(String what, DebugInfo debugInfo) {
        return LuaJava.getInfo(nativeLua,what,debugInfo);
    }

    @Override
    public int getGlobal(String key) {
        return LuaJava.getGlobal(nativeLua,key);
    }

    @Override
    public int getField(int tableIndex, String key) {
        return LuaJava.getField(nativeLua,tableIndex,key);
    }

    @Override
    public void setI(int tableIndex, long n) {
        LuaJava.setI(nativeLua,tableIndex,n);
    }

    @Override
    public void setTable(int tableIndex) {
        LuaJava.setTable(nativeLua,tableIndex);
    }

    @Override
    public void setField(int tableIndex, String key) {
        LuaJava.setField(nativeLua,tableIndex,key);
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
