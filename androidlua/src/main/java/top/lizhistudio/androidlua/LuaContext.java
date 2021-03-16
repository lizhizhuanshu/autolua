package top.lizhistudio.androidlua;


import androidx.annotation.NonNull;

public interface LuaContext {
    int LUA_MAXSTACK = 1000000;
    int LUA_REGISTRYINDEX = -1001000;
    int LUA_RIDX_MAINTHREAD = 1;
    int LUA_RIDX_GLOBALS = 2;

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

    int LUA_MULTRET = -1;
    int LUA_OK = 0;
    int LUA_YIELD = 1;
    int LUA_ERRRUN = 2;
    int LUA_ERRSYNTAX = 3;
    int LUA_ERRMEM = 4;
    int LUA_ERRGCMM = 5;
    int LUA_ERRERR = 6;


    enum CODE_TYPE{
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
    long toPointer(int index);
    long toInteger(int index);
    double toNumber(int index);
    String toString(int index);
    byte[] toBytes(int index);
    boolean toBoolean(int index);
    Object toJavaObject(int index);
    Object toJavaObject(int index,Class<?> aClass);
    Object[] toJavaObjects(int originIndex,Class<?>[] classes);
    Object[] toJavaObjects(int originIndex,int sum);

    void push(Class<?> aClass,Object o);
    void push(Class<?> aClass);
    void push(Object o);
    void push(LuaHandler luaHandler);
    void push(long v);
    void push(double v);
    void push(boolean v);
    void push(byte[] v);
    void push(String v);
    void pushNil();
    void pushJavaObjectMethod();

    int loadBuffer(byte[] code,String chunkName,CODE_TYPE mode);
    int loadFile(String filePath,CODE_TYPE mode);
    int reference(int tableIndex);
    void unReference(int tableIndex,int reference);
    int pCall(int argSum,int resultSum,int errorHandlerIndex);

    void setGlobal(String key);

    int type(int index);
    boolean isInteger(int index);
    void destroy();

    void pop(int n);
    int getTop();
    void setTop(int index);
    boolean isJavaObject(int index);
    boolean getStack(int level,DebugInfo debugInfo);
    boolean getInfo(String what,DebugInfo debugInfo);
    int getGlobal(String key);
    int getField(int tableIndex,String key);

    void setI(int tableIndex,long n);
    void setTable(int tableIndex);
    void setField(int tableIndex,String key);





    Object[] execute(byte[] code,String chunkName,int errorHandlerIndex);
    Object[] executeFile(String path,int errorHandlerIndex);
    Object[] execute(byte[] code,String chunkName,@NonNull LuaHandler errorHandler);
    Object[] executeFile(String path,@NonNull LuaHandler errorHandler);
    Object[] execute(byte[] code);
    Object[] executeFile(String path);
    Object[] execute(String code);

    void setGlobal(String key,Class<?> aClass, Object javaObject);
    void setGlobal(String key,LuaHandler luaHandler);
    void setGlobal(String key,Class<?> aClass);

    int require(String modeName);

    <T> void tableToStruct(int tableIndex,@NonNull T object) throws IllegalAccessException;

    String coerceToString(int index);
}
