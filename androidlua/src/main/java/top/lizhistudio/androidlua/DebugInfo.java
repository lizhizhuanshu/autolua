package top.lizhistudio.androidlua;

public class DebugInfo {
    private long nativePrint;
    public DebugInfo()
    {
        nativePrint = newDebugInfo();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized (this)
        {
            if (nativePrint >0)
            {
                release(nativePrint);
                nativePrint = 0;
            }
        }
    }

    private static native long newDebugInfo();
    private static native void release(long nativePrint);
    private static native String getName(long nativePrint);
    private static native String getNameWhat(long nativePrint);
    private static native String getSource(long nativePrint);
    private static native String getShortSource(long nativePrint);
    private static native String getWhat(long nativePrint);
    private static native boolean isTailCall(long nativePrint);
    private static native boolean isVarArg(long nativePrint);
    private static native int getCurrentLine(long nativePrint);
    private static native int getLastLineDefined(long nativePrint);
    private static native int getLineDefined(long nativePrint);
    private static native int getParamsSum(long nativePrint);
    private static native int getUpValueSum(long nativePrint);


    public boolean isTailCall() {
        return isTailCall(nativePrint);
    }

    public boolean isVarArg() {
        return isVarArg(nativePrint);
    }

    public int getCurrentLine() {
        return getCurrentLine(nativePrint);
    }

    public int getLastLineDefined() {
        return getLastLineDefined(nativePrint);
    }

    public int getLineDefined() {
        return getLineDefined(nativePrint);
    }

    public int getParamsSum() {
        return getParamsSum(nativePrint);
    }

    public int getUpValueSum() {
        return getUpValueSum(nativePrint);
    }

    public String getNameWhat() {
        return getNameWhat(nativePrint);
    }

    public String getShortSource() {
        return getShortSource(nativePrint);
    }

    public String getSource() {
        return getSource(nativePrint);
    }

    public String getWhat() {
        return getWhat(nativePrint);
    }

    public String getName(){
        return getName(nativePrint);
    }
}

