package top.lizhistudio.app.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
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

import top.lizhistudio.androidlua.LuaAdapter;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaFunctionAdapter;
import top.lizhistudio.androidlua.NotReleaseLuaFunctionAdapter;
import top.lizhistudio.autolua.core.value.LuaValue;


public class UserInterfaceImplement implements UserInterface {
    private static final String TAG = "UI";
    private Context context;
    private final LinkedBlockingQueue<LuaValue[]> blockingQueue;
    private final ConcurrentHashMap<String, WeakReference<FloatView>> floatViewCache;
    private final LuaFunctionAdapter registrar;
    private UserInterfaceImplement(){
        blockingQueue= new LinkedBlockingQueue<>();
        floatViewCache = new ConcurrentHashMap<>();
        registrar = new NotReleaseLuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                luaContext.push(new UILuaObjectAdapter(UserInterfaceImplement.this));
                luaContext.setGlobal("UI");
                luaContext.createTable(0,4);
                luaContext.push("RGBA_8888");
                luaContext.push(PixelFormat.RGBA_8888);
                luaContext.setTable(-3);
                luaContext.push("RGB_888");
                luaContext.push(PixelFormat.RGB_888);
                luaContext.setTable(-3);
                luaContext.push("RGB_565");
                luaContext.push(PixelFormat.RGB_565);
                luaContext.setTable(-3);
                luaContext.setGlobal("PixelFormat");
                luaContext.createTable(0,2);
                luaContext.push("FLAG_NOT_FOCUSABLE");
                luaContext.push(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                luaContext.setTable(-3);
                luaContext.push("FLAG_KEEP_SCREEN_ON");
                luaContext.push(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                luaContext.setTable(-3);
                luaContext.setGlobal("LayoutParamsFlags");
                return 0;
            }
        };
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
                            return;
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
    public LuaValue[] takeSignal() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public FloatView getFloatView(String name) {
        WeakReference<FloatView> floatView = floatViewCache.get(name);
        FloatView r = null;
        if (floatView != null)
            r = floatView.get();
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
    public void putSignal(LuaValue[] message) throws InterruptedException {
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


    public LuaFunctionAdapter getRegistrar()
    {
        return registrar;
    }
}