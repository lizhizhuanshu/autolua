package top.lizhistudio.app.core;

import android.content.Context;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.immomo.mls.InitData;
import com.immomo.mls.MLSInstance;
import com.immomo.mls.utils.MainThreadExecutor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class FloatViewImplement implements UserInterface.FloatView {
    private final String name;
    private Context context;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private final MLSInstance mlsInstance ;
    private final AtomicBoolean isDestroy = new AtomicBoolean(false);
    private final AtomicBoolean isShow = new AtomicBoolean(false);
    private final Runnable showRunnable;
    private final Runnable concealRunnable;
    private final Runnable updateRunnable;
    private final Runnable destroyRunnable;
    public FloatViewImplement(String name, Context context,
                              WindowManager.LayoutParams layoutParams,
                              FrameLayout frameLayout,
                              MLSInstance mlsInstance)
    {
        this.name = name;
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager)this.context.
                getSystemService(Context.WINDOW_SERVICE);
        this.layoutParams = layoutParams;
        this.mlsInstance = mlsInstance;
        showRunnable = new Runnable() {
            @Override
            public void run() {
                mlsInstance.onResume();
                windowManager.addView(frameLayout,layoutParams);
            }
        };
        concealRunnable = new Runnable() {
            @Override
            public void run() {
                mlsInstance.onPause();
                windowManager.removeView(frameLayout);
            }
        };
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                windowManager.updateViewLayout(frameLayout,layoutParams);
            }
        };

        destroyRunnable = new Runnable() {
            @Override
            public void run() {
                if (isShow.compareAndSet(true,false))
                    windowManager.removeView(frameLayout);
                mlsInstance.onDestroy();
            }
        };
    }

    private void checkDestroy()
    {
        if (isDestroy.get())
            throw new RuntimeException("FloatView@"+hashCode()+" destroyed");

    }

    @Override
    public void show() {
        checkDestroy();
        if (isShow.compareAndSet(false,true))
        {
            MainThreadExecutor.post(showRunnable);
        }
    }

    @Override
    public void conceal() {
        checkDestroy();
        if (isShow.compareAndSet(true,false))
        {
            MainThreadExecutor.post(concealRunnable);
        }
    }



    @Override
    public void setXY(int x, int y) {
        layoutParams.x = x;
        layoutParams.y = y;
        MainThreadExecutor.post(updateRunnable);
    }

    @Override
    public void setWidthHeight(int width, int height) {
        layoutParams.width = width;
        layoutParams.height = height;
        MainThreadExecutor.post(updateRunnable);
    }

    @Override
    public int getX() {
        return layoutParams.x;
    }

    @Override
    public int getY() {
        return layoutParams.y;
    }

    @Override
    public int getWidth() {
        return layoutParams.width;
    }

    @Override
    public int getHeight() {
        return layoutParams.height;
    }

    @Override
    public boolean reload(String uri) {
        mlsInstance.setData(new InitData(uri));
        return mlsInstance.isValid();
    }

    @Override
    public void destroy() {
        if (isDestroy.compareAndSet(false,true))
        {
            context = null;
            MainThreadExecutor.post(destroyRunnable);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }
}
