package top.lizhistudio.androidlua;

public class DebugInfo {
    private long nativePrint;
    public DebugInfo()
    {
        nativePrint = LuaJava.newDebugInfo();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized (this)
        {
            if (nativePrint >0)
            {
                LuaJava.releaseDebugInfo(nativePrint);
                nativePrint = 0;
            }
        }
    }


    public boolean isTailCall() {
        return LuaJava.isDebugInfoTailCall(nativePrint);
    }

    public boolean isVarArg() {
        return LuaJava.isDebugInfoVarArg(nativePrint);
    }

    public int getCurrentLine() {
        return LuaJava.getDebugInfoCurrentLine(nativePrint);
    }

    public int getLastLineDefined() {
        return LuaJava.getDebugInfoLastLineDefined(nativePrint);
    }

    public int getLineDefined() {
        return LuaJava.getDebugInfoLineDefined(nativePrint);
    }

    public int getParamsSum() {
        return LuaJava.getDebugInfoParamsSum(nativePrint);
    }

    public int getUpValueSum() {
        return LuaJava.getDebugInfoUpValueSum(nativePrint);
    }

    public String getNameWhat() {
        return LuaJava.getDebugInfoNameWhat(nativePrint);
    }

    public String getShortSource() {
        return LuaJava.getDebugInfoShortSource(nativePrint);
    }

    public String getSource() {
        return LuaJava.getDebugInfoSource(nativePrint);
    }

    public String getWhat() {
        return LuaJava.getDebugInfoWhat(nativePrint);
    }

    public String getName(){
        return LuaJava.getDebugInfoName(nativePrint);
    }
}

