package top.lizhistudio.androidlua;


import top.lizhistudio.androidlua.exception.LuaTypeError;

public class LuaJava {

    private LuaJava(){}

    static {
        System.loadLibrary("luajava");
    }

    static native long newLuaState(LuaContextImplement context);
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

}
