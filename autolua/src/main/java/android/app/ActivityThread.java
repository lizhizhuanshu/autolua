package android.app;

import android.content.Context;

public class ActivityThread {
    public Application getApplication(){
        throw new RuntimeException();
    }
    public static ActivityThread systemMain() {
        throw new RuntimeException();
    }

    public static ActivityThread currentActivityThread() {
        throw new RuntimeException();
    }

    public static Application currentApplication() {
        throw new RuntimeException();
    }

    public Context getSystemContext()
    {
        throw new RuntimeException();
    }

    public static void main(String[] args){
        throw new RuntimeException();
    }

}
