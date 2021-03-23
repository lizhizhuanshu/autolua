package top.lizhistudio.autolua.extend;

import android.graphics.Point;
import android.util.LongSparseArray;

import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;
import top.lizhistudio.autolua.conceal.IWindowManager;

public class Screen {
    public static final int  VERTICAL = 1;
    public static final int  LEVEL = -1;

    private static final Point baseSize= new Point();
    private static final int defaultDirection;
    private static final LongSparseArray<Display> displays = new LongSparseArray<>();
    static {
        IWindowManager.getBaseDisplaySize(IWindowManager.MAIN_DISPLAY_TOKEN, baseSize);
        defaultDirection = baseSize.x >baseSize.y ? LEVEL:VERTICAL;
        System.loadLibrary("screen");
    }
    private Screen(){}

    public static int getBaseWidth()
    {
        return baseSize.x;
    }

    public static int getBaseHeight()
    {
        return baseSize.y;
    }

    public static int getBaseDirection()
    {
        return defaultDirection;
    }


    public static int getRotation()
    {
        return IWindowManager.getRotation();
    }

    public static int getBaseDensity()
    {
        return IWindowManager.getBaseDisplayDensity(IWindowManager.MAIN_DISPLAY_TOKEN);
    }

    public static native void injectModel(long nativeLua,Display display);

    public static void injectModel(long nativeLua)
    {
        synchronized (displays)
        {
            Display display = displays.get(nativeLua);
            if (display != null)
                return;
            display = new Display();
            displays.put(nativeLua,display);
            injectModel(nativeLua,display);
        }
    }

    @NativeLuaUseMethod
    public static void releaseDisplay(long nativeLua)
    {
        synchronized (displays)
        {
            displays.remove(nativeLua);
        }
    }

    public static Display getDisplay(long nativeLua)
    {
        synchronized (displays)
        {
            return displays.get(nativeLua);
        }
    }

    public static void clearDisplay()
    {
        synchronized (displays)
        {
            displays.clear();
        }
    }

}
