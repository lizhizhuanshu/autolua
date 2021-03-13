package top.lizhistudio.app.core.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.WindowManager;

import com.immomo.mls.InitData;
import com.immomo.mls.MLSInstance;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class UIImplement implements UI {
    private Context context;
    private final LinkedBlockingQueue<Object> blockingQueue;
    private final ConcurrentHashMap<String, WeakReference<FloatView>> floatViewCache;
    private UIImplement(){
        blockingQueue= new LinkedBlockingQueue<>();
        floatViewCache = new ConcurrentHashMap<>();
    }

    @Override
    public FloatView newFloatView(String name,String uri, WindowManager.LayoutParams layoutParams) {
        MLSInstance instance = new MLSInstance(context,false,false);
        instance.setData(new InitData(uri));
        if (!instance.isValid())
            return null;
        FloatView r = new FloatViewImplement(name,context,layoutParams,instance);
        floatViewCache.put(name,new WeakReference<>(r));
        return r;
    }

    @Override
    public Object takeMessage() throws InterruptedException {
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
    public void putMessage(Object message) throws InterruptedException {
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
