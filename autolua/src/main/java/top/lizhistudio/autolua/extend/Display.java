package top.lizhistudio.autolua.extend;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.LongSparseArray;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.androidlua.annotation.NativeLuaUseMethod;
import top.lizhistudio.autolua.conceal.IWindowManager;
import top.lizhistudio.autolua.conceal.SurfaceControlWrap;

public class Display {
    public static final int  VERTICAL = 1;
    public static final int  LEVEL = -1;
    private static final Point baseSize= new Point();
    private static final int defaultDirection;
    private static final LongSparseArray<Display> displays = new LongSparseArray<>();
    static {
        IWindowManager.getBaseDisplaySize(IWindowManager.MAIN_DISPLAY_TOKEN, baseSize);
        defaultDirection = baseSize.x >baseSize.y ? LEVEL:VERTICAL;
    }
    private final AtomicBoolean isDestroy = new AtomicBoolean(false);
    private int recordDirection;
    private final IBinder iBinder;
    private final Point nowSize;
    private final HandlerThread handlerThread;
    private final Handler handler;
    private ImageReader imageReader;
    private Image nowImage = null;


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

    private Display()
    {
        iBinder = SurfaceControlWrap.createDisplay("Display@"+hashCode(),false);
        nowSize = new Point();
        imageReader = null;
        handlerThread = new HandlerThread("worker@"+hashCode());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public static Display newInstance(long nativeLua)
    {
        Display result = new Display();
        synchronized (displays)
        {
            displays.put(nativeLua,result);
        }
        return result;
    }


    public synchronized void destroy()
    {
        if (isDestroy.compareAndSet(false,true))
        {
            SurfaceControlWrap.destroyDisplay(iBinder);
            if(nowImage != null)
                nowImage.close();
            if(imageReader != null)
                imageReader.close();
            handlerThread.interrupt();
        }
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    private int getDirection()
    {
        return IWindowManager.getRotation()%2 == 0 ? getBaseDirection(): -getBaseDirection();
    }

    public boolean isChangeDirection()
    {
        return getDirection() != recordDirection;
    }

    public synchronized void reset(int width, int height) throws InterruptedException
    {
        recordDirection = getDirection();
        int baseWidth = getBaseWidth();
        int baseHeight = getBaseHeight();
        int baseDirection = getBaseDirection();
        if (width <=0 && height<=0)
        {
            if(recordDirection != baseDirection)
            {
                width = baseHeight;
                height = baseWidth;
            }else
            {
                width = baseWidth;
                height = baseHeight;
            }
        }
        int baseX,baseY;
        if(recordDirection != baseDirection)
        {
            baseX = baseHeight;
            baseY = baseWidth;
        }else
        {
            baseX = baseWidth;
            baseY = baseHeight;
        }
        nowSize.x = width;
        nowSize.y = height;
        if(imageReader != null)
            imageReader.close();
        nowImage = null;
        imageReader = ImageReader.newInstance(width,height, PixelFormat.RGBA_8888,3);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                synchronized (isDestroy)
                {
                    isDestroy.notifyAll();
                }
            }
        },handler);

        SurfaceControlWrap.openTransaction();
        try{
            SurfaceControlWrap.setDisplaySurface(iBinder,imageReader.getSurface());
            SurfaceControlWrap.setDisplayLayerStack(iBinder,0);
            SurfaceControlWrap.setDisplaySize(iBinder,width,height);
            SurfaceControlWrap.setDisplayProjection(iBinder,0,new Rect(0,0, baseX, baseY),new Rect(0,0,width,height));
        }finally {
            SurfaceControlWrap.closeTransaction();
        }
        update();
    }



    public synchronized void update() throws InterruptedException
    {
        Image image;
        synchronized (isDestroy)
        {
            image = imageReader.acquireLatestImage();
            if (image == null)
            {
                if (nowImage == null)
                {
                    isDestroy.wait();
                    nowImage = imageReader.acquireLatestImage();
                }
            }else {
                if (nowImage != null)
                    nowImage.close();
                nowImage = image;
            }
        }
    }

    public synchronized ByteBuffer getDisplayBuffer() throws InterruptedException
    {
        if (nowImage == null)
            update();
        Image.Plane plane = nowImage.getPlanes()[0];
        return plane.getBuffer();
    }

    private ByteBuffer getLocalDisplayBuffer() throws InterruptedException
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getDisplayBuffer().capacity());
        return byteBuffer.put((ByteBuffer) getDisplayBuffer().position(0));
    }


    public ByteBuffer getDisplayBuffer(int x, int y, int x1, int y1) throws InterruptedException
    {
        int pixelStride = getPixelStride();
        int rowStride = getRowStride();
        int width = x1-x+1;
        int height = y1-y+1;
        int resultRowStride = width*pixelStride;
        byte[] bytes = getLocalDisplayBuffer().array();
        ByteBuffer result = ByteBuffer.allocate(resultRowStride*height);
        byte[] resultBytes = result.array();
        for(int h=0;h<height;h++)
        {
            System.arraycopy(
                    bytes
                    ,(y+h)*rowStride+x*pixelStride
                    ,resultBytes
                    ,resultRowStride*h
                    ,resultRowStride);
        }
        return result;
    }

    public Bitmap getBitmap(int x, int y, int x1, int y1) throws InterruptedException
    {
        Bitmap source = Bitmap.createBitmap(getRowStride() / getPixelStride(), getHeight(), Bitmap.Config.ARGB_8888);
        source.copyPixelsFromBuffer(getDisplayBuffer());
        int width = x1 - x + 1;
        int height = y1 - y + 1;
        return Bitmap.createBitmap(source, x, y, width, height);
    }


    public static boolean writeFile(String name , Bitmap bitmap)
    {
        File file = new File(name);
        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        }catch (FileNotFoundException e)
        {
            e.printStackTrace(System.err);
        }
        return  false;
    }

    public boolean writeFile(String name, int x, int y, int x1, int y1) throws InterruptedException
    {
        Bitmap bitmap = getBitmap(x, y, x1, y1);
        return writeFile(name,bitmap);
    }

    public int getWidth(){
        return nowSize.x;
    }

    public int getHeight(){
        return nowSize.y;
    }

    public int getRowStride(){
        return nowImage.getPlanes()[0].getRowStride();
    }

    public int getPixelStride(){
        return  nowImage.getPlanes()[0].getPixelStride();
    }

}
