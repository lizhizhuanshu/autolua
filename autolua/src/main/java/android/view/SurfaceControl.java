

package android.view;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;


public class SurfaceControl {

    public static final int BUILT_IN_DISPLAY_ID_MAIN = 0;

    public static final int BUILT_IN_DISPLAY_ID_HDMI = 1;

    public static void openTransaction() {
        throw new RuntimeException();
    }

    public static void closeTransaction() {
        throw new RuntimeException();
    }

    public static int getActiveConfig(IBinder displayToken) {
        throw new RuntimeException();
    }

    public static boolean setActiveConfig(IBinder displayToken, int id) {
        throw new RuntimeException();
    }

    public static void setDisplayProjection(IBinder displayToken,
                                            int orientation, Rect layerStackRect, Rect displayRect) {
        throw new RuntimeException();
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        throw new RuntimeException();
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        throw new RuntimeException();
    }

    public static void setDisplaySize(IBinder displayToken, int width, int height) {
        throw new RuntimeException();
    }

    public static IBinder createDisplay(String name, boolean secure) {
        throw new RuntimeException();
    }

    public static void destroyDisplay(IBinder displayToken) {
        throw new RuntimeException();
    }

    public static IBinder getBuiltInDisplay(int builtInDisplayId) {
        throw new RuntimeException();
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
        throw new RuntimeException();
    }


    public static Bitmap screenshot(int width, int height) {
        throw new RuntimeException();
    }

    private static void screenshot(IBinder display, Surface consumer, Rect sourceCrop,
                                   int width, int height, int minLayer, int maxLayer, boolean allLayers,
                                   boolean useIdentityTransform) {
        throw new RuntimeException();
    }
}
