package top.lizhistudio.app.core.ui;

import android.content.Context;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.immomo.mls.InitData;
import com.immomo.mls.MLSInstance;
import com.immomo.mls.utils.MainThreadExecutor;

import java.util.concurrent.atomic.AtomicInteger;

import top.lizhistudio.app.core.ui.FloatView;

public class FloatViewImplement implements FloatView {
    private final static int DESTROYED = 0;
    private final static int CONCEAL = 1;
    private final static int SHOW = 2;
    private final String name;
    private Context context;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;
    private FrameLayout frameLayout;
    private final MLSInstance mlsInstance ;
    private final AtomicInteger state = new AtomicInteger(0);

    private final Runnable showRunnable;
    private final Runnable concealRunnable;
    private final Runnable updateRunnable;
    public FloatViewImplement(String name, Context context,
                              WindowManager.LayoutParams layoutParams,
                              MLSInstance mlsInstance)
    {
        this.name = name;
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager)this.context.
                getSystemService(Context.WINDOW_SERVICE);
        this.layoutParams = layoutParams;
        this.frameLayout = new FrameLayout(this.context);
        this.mlsInstance = mlsInstance;
        this.mlsInstance.setContainer(frameLayout);

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
    }

    @Override
    public void show() {
        if (state.compareAndSet(CONCEAL,SHOW))
        {
            MainThreadExecutor.post(showRunnable);
        }
    }

    @Override
    public void conceal() {
        if (state.compareAndSet(SHOW,CONCEAL))
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
        if (state.compareAndSet(CONCEAL,DESTROYED))
        {
            frameLayout = null;
            context = null;
            MainThreadExecutor.post(new Runnable() {
                @Override
                public void run() {
                    mlsInstance.onDestroy();
                }
            });
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
