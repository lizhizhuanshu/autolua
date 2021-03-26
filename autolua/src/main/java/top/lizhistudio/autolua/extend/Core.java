package top.lizhistudio.autolua.extend;

public class Core {
    private Core(){}

    static {
        System.loadLibrary("autolua");
    }

    private static native void inject(long nativeLua, Display display);


    public static void inject(long nativeLua)
    {
        Display display = Display.newInstance(nativeLua);
        inject(nativeLua,display);
    }
}
