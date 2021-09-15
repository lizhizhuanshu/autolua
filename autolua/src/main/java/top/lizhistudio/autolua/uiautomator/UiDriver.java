package top.lizhistudio.autolua.uiautomator;


import android.app.UiAutomation;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeoutException;


import top.lizhistudio.androidlua.CommonLuaObjectAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.autolua.uiautomator.filter.UiSelector;

public class UiDriver extends SearchableLuaObject{
    private static final int MAX_CONNECT_SUM = 100;
    private UiAutomation uiAutomation = null;
    private HandlerThread handlerThread = null;

    private UiDriver()
    {

    }


    public int newSelector(LuaContext L)
    {
        L.push(new UiSelector());
        return 1;
    }

    public int waitForIdle(LuaContext L)
    {
        long time = L.toLong(2);
        long time1 = L.toLong(3);
        boolean r = true;
        try {
            uiAutomation.waitForIdle(time,time1);
        }catch (TimeoutException e)
        {
            r = false;
        }
        L.push(r);
        return 1;
    }

    private AccessibilityNodeInfo mCache = null;

    private AccessibilityNodeInfo getNodeInfo()
    {
        AccessibilityNodeInfo r;
        while (true)
        {
            r = uiAutomation.getRootInActiveWindow();
            if (r!= null)
                break;
            SystemClock.sleep(60);
        }
        return r;
    }

    @Override
    protected synchronized AccessibilityNodeInfo getAccessibilityNodeInfo() {
        if (mCache == null || !mCache.refresh())
            mCache = getNodeInfo();
        return mCache;
    }

    private static final class Stub{
        private static final UiDriver stub = new UiDriver();
    }

    public static UiDriver getInstance()
    {
        return Stub.stub;
    }

    public synchronized boolean connect()
    {
        handlerThread = new HandlerThread("UiAutomator");
        handlerThread.start();
        try{
            Class<?> clazz = Class.forName("android.app.UiAutomationConnection");
            Object connection = clazz.newInstance();
            Constructor<UiAutomation> constructor =
                    UiAutomation.class.getConstructor(Looper.class,Class.forName("android.app.IUiAutomationConnection"));
            for (int i = 0; i < MAX_CONNECT_SUM; i++) {
                uiAutomation = constructor.newInstance(handlerThread.getLooper(),connection);
                UiAutomation.class.getMethod("connect",int.class).invoke(uiAutomation,1);
                if (uiAutomation.getRootInActiveWindow()!=null)
                {
                    System.err.println("UiAutomation "+(i+1)+" connect right");
                    return true;
                }
                else
                    UiAutomation.class.getMethod("disconnect").invoke(uiAutomation);
            }
        }catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
        handlerThread.quit();
        handlerThread = null;
        uiAutomation = null;
        return false;
    }

    public synchronized void disconnect()
    {
        if (handlerThread!=null)
        {
            try {
                UiAutomation.class.getMethod("disconnect").invoke(uiAutomation);
            }catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            uiAutomation = null;
            handlerThread = null;
        }
    }

}
