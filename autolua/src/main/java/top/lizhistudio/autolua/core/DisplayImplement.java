package top.lizhistudio.autolua.core;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.autolua.conceal.IWindowManager;
import top.lizhistudio.autolua.conceal.SurfaceControlWrap;

public class DisplayImplement implements Display{

    public static final int  VERTICAL = 1;
    public static final int  LEVEL = -1;
    private static final Point baseSize= new Point();
    private static final int defaultDirection;
    static {
        IWindowManager.getBaseDisplaySize(IWindowManager.MAIN_DISPLAY_TOKEN, baseSize);
        defaultDirection = baseSize.x >baseSize.y ? LEVEL:VERTICAL;
    }
    private final AtomicBoolean isDestroy = new AtomicBoolean(false);
    private final Object imageMutex = new Object();
    private int recordDirection;
    private IBinder iBinder;
    private final Point nowSize;
    private final HandlerThread handlerThread;
    private final Handler handler;
    private ImageReader imageReader;
    private Image nowImage = null;

    public DisplayImplement(int width,int height)
    {
        nowSize = new Point();
        imageReader = null;
        handlerThread = new HandlerThread("worker@"+hashCode());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public DisplayImplement()
    {
        this(0,0);
    }

    @SuppressLint("WrongConstant")
    public synchronized boolean initialize(int width, int height)
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
                synchronized (imageMutex)
                {
                    imageMutex.notifyAll();
                }
            }
        },handler);

        SurfaceControlWrap.openTransaction();
        try{
            boolean secure = Build.VERSION.SDK_INT < Build.VERSION_CODES.R || (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && !"S"
                    .equals(Build.VERSION.CODENAME));
            if (iBinder == null)
                iBinder = SurfaceControlWrap.createDisplay("AutoLua",secure);
            SurfaceControlWrap.setDisplaySurface(iBinder,imageReader.getSurface());
            SurfaceControlWrap.setDisplayLayerStack(iBinder,0);
            SurfaceControlWrap.setDisplaySize(iBinder,width,height);
            SurfaceControlWrap.setDisplayProjection(iBinder,0,new Rect(0,0, baseX, baseY),new Rect(0,0,width,height));
        }finally {
            SurfaceControlWrap.closeTransaction();
        }
        update();
        return true;
    }


    public int getBaseWidth()
    {
        return baseSize.x;
    }

    public int getBaseHeight()
    {
        return baseSize.y;
    }

    public int getBaseDirection()
    {
        return defaultDirection;
    }


    public int getRotation()
    {
        return IWindowManager.getRotation();
    }

    public int getBaseDensity()
    {
        return IWindowManager.getBaseDisplayDensity(IWindowManager.MAIN_DISPLAY_TOKEN);
    }

    @Override
    public void destroy() {
        if (isDestroy.compareAndSet(false,true))
        {
            handlerThread.interrupt();
            if (iBinder != null)
            {
                SurfaceControlWrap.destroyDisplay(iBinder);
                iBinder = null;
            }
            if(nowImage != null)
                nowImage.close();
            if(imageReader != null)
                imageReader.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    public int getDirection()
    {
        return IWindowManager.getRotation()%2 == 0 ? getBaseDirection(): -getBaseDirection();
    }

    public boolean isChangeDirection()
    {
        return getDirection() != recordDirection;
    }


    @Override
    public void update() {
        if (iBinder == null)
            initialize(0,0);
        Image image;
        try{
            synchronized (imageMutex)
            {
                image = imageReader.acquireLatestImage();
                if (image == null)
                {
                    if (nowImage == null)
                    {
                        imageMutex.wait();
                        nowImage = imageReader.acquireLatestImage();
                    }
                }else {
                    if (nowImage != null)
                        nowImage.close();
                    nowImage = image;
                }
            }
        }catch (InterruptedException e)
        {
            throw  new RuntimeException(e);
        }

    }

    @Override
    public ByteBuffer getDisplayBuffer() {
        if (nowImage == null)
            update();
        Image.Plane plane = nowImage.getPlanes()[0];
        return plane.getBuffer();
    }


    public int getWidth(){
        return nowSize.x;
    }

    public int getHeight(){
        return nowSize.y;
    }

    public int getRowStride(){
        if (nowImage == null)
            update();
        return nowImage.getPlanes()[0].getRowStride();
    }

    public int getPixelStride(){
        if (nowImage == null)
            update();
        return  nowImage.getPlanes()[0].getPixelStride();
    }

}
