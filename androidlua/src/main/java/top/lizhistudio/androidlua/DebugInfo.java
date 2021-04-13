package top.lizhistudio.androidlua;

public class DebugInfo {
    private long nativePrint;
    public DebugInfo()
    {
        nativePrint = LuaContext.newDebugInfo();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized (this)
        {
            if (nativePrint >0)
            {
                LuaContext.releaseDebugInfo(nativePrint);
                nativePrint = 0;
            }
        }
    }


    public boolean isTailCall() {
        return LuaContext.isDebugInfoTailCall(nativePrint);
    }

    public boolean isVarArg() {
        return LuaContext.isDebugInfoVarArg(nativePrint);
    }

    public int getCurrentLine() {
        return LuaContext.getDebugInfoCurrentLine(nativePrint);
    }

    public int getLastLineDefined() {
        return LuaContext.getDebugInfoLastLineDefined(nativePrint);
    }

    public int getLineDefined() {
        return LuaContext.getDebugInfoLineDefined(nativePrint);
    }

    public int getParamsSum() {
        return LuaContext.getDebugInfoParamsSum(nativePrint);
    }

    public int getUpValueSum() {
        return LuaContext.getDebugInfoUpValueSum(nativePrint);
    }

    public String getNameWhat() {
        return LuaContext.getDebugInfoNameWhat(nativePrint);
    }

    public String getShortSource() {
        return LuaContext.getDebugInfoShortSource(nativePrint);
    }

    public String getSource() {
        return LuaContext.getDebugInfoSource(nativePrint);
    }

    public String getWhat() {
        return LuaContext.getDebugInfoWhat(nativePrint);
    }

    public String getName(){
        return LuaContext.getDebugInfoName(nativePrint);
    }
}

