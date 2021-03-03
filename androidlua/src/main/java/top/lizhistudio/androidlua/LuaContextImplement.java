package top.lizhistudio.androidlua;

import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import top.lizhistudio.androidlua.exception.LuaTypeError;

public class LuaContextImplement implements LuaContext {
    private static final String TAG = "LuaContext";
    private long nativeLua;
    private final LongSparseArray<JavaObjectWrapper> javaObjectWrappers;
    private final JavaObjectWrapperFactory javaObjectWrapperFactory;

    public LuaContextImplement(@NonNull JavaObjectWrapperFactory javaObjectWrapperFactory)
    {
        nativeLua = LuaJava.newLuaState(this);
        javaObjectWrappers = new LongSparseArray<>();
        this.javaObjectWrapperFactory = javaObjectWrapperFactory;
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
        throw new LuaTypeError("lua "+nativeLua+" index "+index+"can't to Java Object");
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
        if (aClass.isPrimitive())
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

    private void pushObject(Class<?> aClass,Object o)
    {
        if (o == null)
            pushNil();
        else if (aClass.isPrimitive())
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
        }else if (aClass.isAssignableFrom(Number.class)) {
            if (aClass.isAssignableFrom(Integer.class))
                push((int)o);
            else if (aClass.isAssignableFrom(Long.class))
                push((long)o);
            else if (aClass.isAssignableFrom(Float.class))
                push((float)o);
            else if (aClass.isAssignableFrom(Double.class))
                push((double)o);
            else if (aClass.isAssignableFrom(Byte.class))
                push((byte) o);
            else if (aClass.isAssignableFrom(Short.class))
                push((short)o);
        }else if(aClass.isAssignableFrom(Boolean.class))
            push((boolean)o);
        else if(aClass.isAssignableFrom(String.class))
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


    private Object[] getResult(int resultCount)
    {
        try{
            Object[] result = new Object[resultCount];
            for (int i = resultCount; i > 0; i--) {
                result[i-1] = toJavaObject(i);
            }
            return result;
        }finally {
            LuaJava.setTop(nativeLua,getTop()- resultCount);
        }
    }

    @Override
    public Object[] execute(byte[] code, String chunkName) {
        int resultCount = LuaJava.execute(nativeLua,code,chunkName);
        return getResult(resultCount);
    }

    @Override
    public Object[] executeFile(String path) {
        int resultCount = LuaJava.executeFile(nativeLua,path);
        return getResult(resultCount);
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
    protected void finalize() throws Throwable {
        destroy();
    }

    @Override
    public void pushWrapper(long id, JavaObjectWrapper objectWrapper) {
        javaObjectWrappers.put(id,objectWrapper);
    }

    @Override
    public JavaObjectWrapper getWrapper(long id) {
        return javaObjectWrappers.get(id);
    }

    @Override
    public void removeWrapper(long id) {
        javaObjectWrappers.remove(id);
    }

    @Override
    public int getTop() {
        return LuaJava.getTop(nativeLua);
    }

    @Override
    public void setTop(int index) {
        LuaJava.setTop(nativeLua,index);
    }
}
