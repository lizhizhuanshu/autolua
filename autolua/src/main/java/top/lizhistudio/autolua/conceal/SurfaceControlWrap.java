package top.lizhistudio.autolua.conceal;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface;

public class SurfaceControlWrap {
    public static final int BUILT_IN_DISPLAY_ID_MAIN = 0;
    public static final int BUILT_IN_DISPLAY_ID_HDMI = 1;
    private static Class<?> aClass;

    static {
        try{
            aClass = Class.forName("android.view.SurfaceControl");
        }catch (ClassNotFoundException e)
        {
        }
    }

    public static void openTransaction() {
        try{
            aClass.getMethod("openTransaction").invoke(null);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void closeTransaction() {
        try{
            aClass.getMethod("closeTransaction").invoke(null);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static int getActiveConfig(IBinder displayToken) {
        try{
            return (int)aClass.getMethod("getActiveConfig",IBinder.class).invoke(null,displayToken);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static boolean setActiveConfig(IBinder displayToken, int id) {
        try{
            return (boolean)aClass.getMethod("setActiveConfig",IBinder.class,int.class).invoke(null,displayToken,id);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void setDisplayProjection(IBinder displayToken,
                                            int orientation, Rect layerStackRect, Rect displayRect) {
        try{
            aClass.getMethod("setDisplayProjection",IBinder.class,int.class,Rect.class,Rect.class)
                    .invoke(null,displayToken,orientation,layerStackRect,displayRect);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        try{
            aClass.getMethod("setDisplayLayerStack",IBinder.class,int.class)
                    .invoke(null,displayToken,layerStack);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        try{
            aClass.getMethod("setDisplaySurface",IBinder.class,Surface.class)
                    .invoke(null,displayToken,surface);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void setDisplaySize(IBinder displayToken, int width, int height) {
        try{
            aClass.getMethod("setDisplaySize",IBinder.class,int.class,int.class)
                    .invoke(null,displayToken,width,height);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static IBinder createDisplay(String name, boolean secure) {
        try{
            return(IBinder)aClass.getMethod("createDisplay",String.class,boolean.class)
                    .invoke(null,name,secure);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void destroyDisplay(IBinder displayToken) {
        try{
            aClass.getMethod("destroyDisplay",IBinder.class)
                    .invoke(null,displayToken);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static IBinder getBuiltInDisplay(int builtInDisplayId) {
        try{
            return(IBinder)aClass.getMethod("getBuiltInDisplay",int.class)
                    .invoke(null,builtInDisplayId);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    public static void screenshot(IBinder display, Surface consumer,
                                  int width, int height, int minLayer, int maxLayer,
                                  boolean useIdentityTransform) {
        screenshot(display, consumer, new Rect(), width, height, minLayer, maxLayer,
                false, useIdentityTransform);
    }

    public static void screenshot(IBinder display, Surface consumer,
                                  int width, int height) {
        screenshot(display, consumer, new Rect(), width, height, 0, 0, true, false);
    }

    public static void screenshot(IBinder display, Surface consumer) {
        screenshot(display, consumer, new Rect(), 0, 0, 0, 0, true, false);
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height,
                                    int minLayer, int maxLayer, boolean useIdentityTransform,
                                    int rotation) {
        try{
            return(Bitmap) aClass.getMethod("screenshot",Rect.class,int.class,int.class,int.class,int.class,boolean.class,int.class)
                    .invoke(null,sourceCrop,width,height,minLayer,maxLayer,useIdentityTransform,rotation);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }


    public static Bitmap screenshot(int width, int height) {
        try{
            return(Bitmap) aClass.getMethod("screenshot",int.class,int.class)
                    .invoke(null,width,height);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

    private static void screenshot(IBinder display, Surface consumer, Rect sourceCrop,
                                   int width, int height,
                                   int minLayer, int maxLayer,
                                   boolean allLayers, boolean useIdentityTransform) {
        try{
            aClass.getMethod("screenshot",IBinder.class,Surface.class,Rect.class,
                    int.class,int.class,
                    int.class,int.class,
                    boolean.class,boolean.class)
                    .invoke(null,display,consumer,sourceCrop,width,height,minLayer,maxLayer,allLayers,useIdentityTransform);
        }catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }
}
