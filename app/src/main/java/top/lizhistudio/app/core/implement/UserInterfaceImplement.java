package top.lizhistudio.app.core.implement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.service.controls.Control;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.immomo.mls.InitData;
import com.immomo.mls.MLSInstance;
import com.immomo.mls.utils.MainThreadExecutor;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import top.lizhistudio.autolua.core.UserInterface;

public class UserInterfaceImplement implements UserInterface {
    private static final String TAG = "UI";
    private Context context;
    private final LinkedBlockingQueue<Object> blockingQueue;
    private final ConcurrentHashMap<String, WeakReference<FloatView>> floatViewCache;
    UserInterfaceImplement(){
        blockingQueue= new LinkedBlockingQueue<>();
        floatViewCache = new ConcurrentHashMap<>();
    }


    private static void log(String message)
    {
        Log.d("UI",message);
    }


    @Override
    public FloatView newFloatView(String name,String uri, LayoutParams layoutParams) {
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
                            return;
                        WindowManager.LayoutParams rawLayoutParams = layoutParams==null?
                                new WindowManager.LayoutParams():layoutParams.toRawLayoutParams();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            rawLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        } else {
                            rawLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                        }
                        FloatView r = new FloatViewImplement(name,context,rawLayoutParams,frameLayout,instance);
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
        private static final UserInterfaceImplement instance = new UserInterfaceImplement();
    }

    public static UserInterfaceImplement getDefault(){
        return Self.instance;
    }

    public void initialize(Context context)
    {
        this.context = context.getApplicationContext();
    }



}
