package top.lizhistudio.autolua.core;

import android.util.Log;

public class BasePrintListener implements LuaInterpreter.PrintListener {
    @Override
    public void onPrint(String source, int line, String message) {
        Log.d("AutoLuaPrint",source+" : "+line+" : "+message);
    }

    @Override
    public void onErrorPrint(String source, int line, String message) {
        Log.e("AutoLuaErrorPrint",source+" : "+line+" : "+message);
    }
}
