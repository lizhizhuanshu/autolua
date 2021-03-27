package top.lizhistudio.autolua.core;

import android.util.Log;

public class BasePrintListener implements PrintListener {
    @Override
    public void onPrint(String source, int line, String message) {
        Log.d("AutoLuaPrint",source+" : "+line+" : "+message);
    }
}
