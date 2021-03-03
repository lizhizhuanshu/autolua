package top.lizhistudio.androidlua;

import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;

public interface LuaContext {
    int LUA_TNONE = -1;
    int LUA_TNIL = 0;
    int LUA_TBOOLEAN = 1;
    int LUA_TLIGHTUSERDATA = 2;
    int LUA_TNUMBER = 3;
    int LUA_TSTRING = 4;
    int LUA_TTABLE = 5;
    int LUA_TFUNCTION = 6;
    int LUA_TUSERDATA = 7;
    int LUA_TTHREAD = 8;

    long toInteger(int index);
    double toNumber(int index);
    String toString(int index);
    byte[] toBytes(int index);
    boolean toBoolean(int index);
    Object toJavaObject(int index);
    Object toJavaObject(int index,Class<?> aClass);
    Object[] toJavaObjects(int originIndex,Class<?>[] classes);

    void push(Class<?> aClass,Object o);
    void push(Class<?> aClass);
    void push(Object o);
    void push(long v);
    void push(double v);
    void push(boolean v);
    void push(byte[] v);
    void push(String v);
    void pushNil();
    void pushJavaObjectMethod();

    void setGlobal(String key);
    int type(int index);
    boolean isInteger(int index);
    Object[] execute(byte[] code,String chunkName);
    Object[] executeFile(String path);
    void destroy();

    @NativeLuaUseMethod
    void pushWrapper(long id, JavaObjectWrapper objectWrapper);
    @NativeLuaUseMethod
    JavaObjectWrapper getWrapper(long id);
    @NativeLuaUseMethod
    void removeWrapper(long id);


    int getTop();
    void setTop(int index);
}
