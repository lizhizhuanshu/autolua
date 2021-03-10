package top.lizhistudio.autolua.extend;

import android.util.Log;

public class MyLog {
    private static final String TAG = "autoLuaCore";
    private MyLog(){}
    public static void e(String message)
    {
        System.err.println(TAG+"    "+message);
    }
    public static void d(String message)
    {
        System.out.println(TAG+"    "+message);
    }
}
