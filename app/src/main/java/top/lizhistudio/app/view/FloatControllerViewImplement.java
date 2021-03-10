package top.lizhistudio.app.view;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import top.lizhistudio.app.R;

public class FloatControllerViewImplement extends BroadcastReceiver implements FloatControllerView {
    private Context context;
    private OnClickListener onClickListener = null;
    private DisplayMetrics displayMetrics;
    private WindowManager windowManager;
    private ImageButton imageButton;
    private WindowManager.LayoutParams layoutParams;
    private AtomicBoolean showed;
    private Handler mainHandler;
    private IntentFilter filter;
    private int nowImage;

    public FloatControllerViewImplement(Context context, int imageDiameter){
        showed = new AtomicBoolean(false);
        displayMetrics = new DisplayMetrics();
        nowImage = R.mipmap.start;

        this.windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        imageButton = new ImageButton(context);
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = dipToPx(imageDiameter);
        layoutParams.height = dipToPx(imageDiameter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        ControlButtonListener listener = new ControlButtonListener();
        imageButton.setOnClickListener(listener);
        imageButton.setOnTouchListener(listener);
        imageButton.setImageResource(R.mipmap.start);
        mainHandler = new MainHandler(new WeakReference<>(this));
        this.context = context;
        filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
    }


    private int dipToPx(int dip)
    {
        return (int)(displayMetrics.density*dip+0.5f);
    }

    private void directShow(int pixelX,int pixelY){
        if(pixelX >=0 && pixelY >=0)
        {
            layoutParams.x = pixelX;
            layoutParams.y = pixelY;
        }
        if(showed.get())
            windowManager.updateViewLayout(imageButton,layoutParams);
        else{
            context.registerReceiver( this,filter);
            windowManager.addView(imageButton,layoutParams);
            showed.set(true);
        }
    }

    private void directConceal(){
        if(showed.get())
        {
            context.unregisterReceiver(this);
            windowManager.removeView(imageButton);
            showed.set(false);
        }

    }

    private void directSetImage(int id){
        imageButton.setImageResource(id);
        nowImage = id;
    }


    public void setState(int state){
        Message message = Message.obtain(mainHandler);
        message.what = MainHandler.EVENT_SET_IMAGE;
        message.arg1 = state == STOPPED_STATE ? R.mipmap.start:R.mipmap.stop;
        mainHandler.sendMessage(message);
    }

    public void onReceive(Context context, Intent intent) {
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        if(showed.get())
            reShow();
    }

    private void show(int pixelX,int pixelY){
        Message message = Message.obtain(mainHandler);
        message.what = MainHandler.EVENT_SHOW;
        message.arg1 = pixelX;
        message.arg2 = pixelY;
        mainHandler.sendMessage(message);
    }

    public void move(int pixelX,int pixelY){
        int x = 0;
        if(pixelX> displayMetrics.widthPixels/2 )
            x = displayMetrics.widthPixels - layoutParams.width;
        show(x,pixelY);
    }

    public void reShow(){
        show(displayMetrics.widthPixels-layoutParams.width,
                displayMetrics.heightPixels/3);
    }

    @Override
    public void show() {
        show(-1,-1);
    }


    public void conceal(){
        Message message = Message.obtain(mainHandler);
        message.what = MainHandler.EVENT_CONCEAL;
        mainHandler.sendMessage(message);
    }

    private static final class MainHandler extends Handler {
        private static final int EVENT_SHOW = 0;
        private static final int EVENT_CONCEAL = 1;
        private static final int EVENT_SET_IMAGE = 2;
        private WeakReference<FloatControllerViewImplement> floatImageButton;
        private MainHandler(WeakReference<FloatControllerViewImplement> floatImageButton){
            super(Looper.getMainLooper());
            this.floatImageButton = floatImageButton;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case EVENT_SHOW:
                    floatImageButton.get().directShow(msg.arg1,msg.arg2);
                    break;
                case EVENT_CONCEAL:
                    floatImageButton.get().directConceal();
                    break;
                case EVENT_SET_IMAGE:
                    floatImageButton.get().directSetImage(msg.arg1);
                    break;
            }
        }
    }

    public void setOnClickListener(OnClickListener onClick){
        onClickListener = onClick;
    }

    private final class ControlButtonListener implements View.OnClickListener, View.OnTouchListener
    {
        private int buttonX;
        private int buttonY;
        @Override
        public void onClick(View v) {
            if(onClickListener!= null)
                onClickListener.onClick(FloatControllerViewImplement.this,nowImage == R.mipmap.start? STOPPED_STATE : EXECUTEING_STATE);
        }

        private void touchButton(MotionEvent event)
        {
            int nowX = (int) event.getRawX();
            int nowY = (int) event.getRawY();
            int movedX = nowX - buttonX;
            int movedY = nowY - buttonY;
            buttonX = nowX;
            buttonY = nowY;
            int x = layoutParams.x + movedX;
            int y = layoutParams.y + movedY;
            if(x < 0)
                x = 0;
            else
            {
                int maxLocationX = displayMetrics.widthPixels-layoutParams.width;
                if(x > maxLocationX )
                    x = maxLocationX ;
            }
            show(x,y);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    buttonX = (int) event.getRawX();
                    buttonY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchButton(event);
                    break;
                case MotionEvent.ACTION_UP:
                    int x = 0;
                    if(event.getRawX()> displayMetrics.widthPixels/2 )
                        x = displayMetrics.widthPixels - layoutParams.width;
                    show(x,layoutParams.y);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

}
