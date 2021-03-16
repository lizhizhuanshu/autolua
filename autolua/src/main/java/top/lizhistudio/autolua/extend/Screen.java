package top.lizhistudio.autolua.extend;

import android.graphics.Point;

import top.lizhistudio.autolua.conceal.IWindowManager;

public class Screen {
    public static final int  VERTICAL = 1;
    public static final int  LEVEL = -1;

    private static final Point baseSize= new Point();
    private static final int defaultDirection;

    static {
        IWindowManager.getBaseDisplaySize(IWindowManager.MAIN_DISPLAY_TOKEN, baseSize);
        defaultDirection = baseSize.x >baseSize.y ? LEVEL:VERTICAL;
    }
    private Screen(){}

    public static int getWidth()
    {
        return baseSize.x;
    }

    public static int getHeight()
    {
        return baseSize.y;
    }

    public static int getDirection()
    {
        return defaultDirection;
    }

    public static Display newDisplay(int width,int height) throws InterruptedException
    {
        return new Display(width, height);
    }

    public static int getRotation()
    {
        return IWindowManager.getRotation();
    }

    public static int getDensity()
    {
        return IWindowManager.getBaseDisplayDensity(IWindowManager.MAIN_DISPLAY_TOKEN);
    }

}
