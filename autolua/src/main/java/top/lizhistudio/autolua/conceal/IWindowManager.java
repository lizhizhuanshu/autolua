package top.lizhistudio.autolua.conceal;

import android.app.Service;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

import java.lang.reflect.Method;

public final class IWindowManager {
    public static final int MAIN_DISPLAY_TOKEN = 0;
    private static Object iWindowManagerObject ;
    private static Method getBaseDisplaySize ;
    private static Method getBaseDisplayDensity;
    private static Method getRotation;
    static{
        try{
            Class iWindowManager = Class.forName("android.view.IWindowManager");
            Class iWindowManagerStub = Class.forName("android.view.IWindowManager$Stub");
            Method asInterface = iWindowManagerStub.getDeclaredMethod(
                    "asInterface", IBinder.class);
            IBinder iWindowManagerIBinder = ServiceManager.getService(Service.WINDOW_SERVICE);
            iWindowManagerObject = asInterface.invoke(null,iWindowManagerIBinder);
            getBaseDisplaySize = iWindowManager.getDeclaredMethod(
                    "getBaseDisplaySize",int.class, Point.class);
            getBaseDisplayDensity = iWindowManager.getDeclaredMethod(
                    "getBaseDisplayDensity",int.class);
            if(Build.VERSION.SDK_INT >=26)
                getRotation = iWindowManager.getDeclaredMethod("getDefaultDisplayRotation");
            else
                getRotation = iWindowManager.getDeclaredMethod("getRotation");
        }catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    public static void getBaseDisplaySize(int token, Point point)
    {
        try{
            getBaseDisplaySize.invoke(iWindowManagerObject,token,point);
        }catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
    }



    public static int getRotation()
    {
        int result = -1;
        try{
            result =(int)getRotation.invoke(iWindowManagerObject);
        }catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
        return result;
    }
    public static int getBaseDisplayDensity(int displayId)
    {
        int result = -1;
        try{
            result =(int)getBaseDisplayDensity.invoke(iWindowManagerObject,displayId);
        }catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
        return result;
    }
}
