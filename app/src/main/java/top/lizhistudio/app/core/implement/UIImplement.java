package top.lizhistudio.app.core.implement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.immomo.mls.InitData;
import com.immomo.mls.MLSInstance;
import com.immomo.mls.annotation.LuaBridge;
import com.immomo.mls.annotation.LuaClass;
import com.immomo.mls.utils.MainThreadExecutor;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import top.lizhistudio.app.core.AbstractUI;
import top.lizhistudio.app.core.FloatView;
import top.lizhistudio.app.core.UI;

public class UIImplement extends AbstractUI {
    private static final String TAG = "UI";
    private Context context;
    private final LinkedBlockingQueue<Object> blockingQueue;
    private final ConcurrentHashMap<String, WeakReference<FloatView>> floatViewCache;
    private UIImplement(){
        blockingQueue= new LinkedBlockingQueue<>();
        floatViewCache = new ConcurrentHashMap<>();
    }

    private static void log(String message)
    {
        Log.d("UI",message);
    }


    @Override
    public FloatView newFloatView(String name,String uri, WindowManager.LayoutParams layoutParams) {
        AtomicReference<FloatView> result = new AtomicReference<>(null);
        synchronized (result)
        {
            MainThreadExecutor.post(new Runnable() {
                @Override
                public void run() {
                    try{
                        MLSInstance instance = new MLSInstance(context,false,false);
                        FrameLayout frameLayout = new FrameLayout(context);
                        instance.setContainer(frameLayout);
                        instance.setData(new InitData(uri));
                        if (!instance.isValid())
                        {
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        } else {
                            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                        }
                        FloatView r = new FloatViewImplement(name,context,layoutParams,frameLayout,instance);
                        floatViewCache.put(name,new WeakReference<>(r));
                        result.set(r);
                    }finally {
                        synchronized (result)
                        {
                            result.notifyAll();
                        }
                    }
                }
            });
            try{
                result.wait();
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return result.get();
        }
    }

    @Override
    public Object takeSignal() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public FloatView getFloatView(String name) {
        WeakReference<FloatView> floatView = floatViewCache.get(name);
        FloatView r = floatView.get();
        if (r == null)
            floatViewCache.remove(name);
        return r;
    }

    @Override
    public void showMessage(String message,int time) {
        MainThreadExecutor.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message,time).show();
            }
        });
    }

    @Override
    public void putSignal(Object message) throws InterruptedException {
        blockingQueue.put(message);
    }

    private static class Self{
        @SuppressLint("StaticFieldLeak")
        private static final  UIImplement  instance = new UIImplement();
    }

    public static UIImplement getInstance(){
        return Self.instance;
    }

    public void initialize(Context context)
    {
        this.context = context.getApplicationContext();
    }

}
