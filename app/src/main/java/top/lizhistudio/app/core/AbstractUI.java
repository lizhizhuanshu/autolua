package top.lizhistudio.app.core;

import android.os.Parcel;
import android.view.WindowManager;

public abstract class AbstractUI implements UI {
    public FloatView newFloatView(String name, String uri,byte[] layoutParamsBuffer)
    {
        Parcel parcel = Parcel.obtain();
        try {
            parcel.unmarshall(layoutParamsBuffer,0,layoutParamsBuffer.length);
            parcel.setDataPosition(0);
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)
                    parcel.readValue(Thread.currentThread().getContextClassLoader());
            return newFloatView(name, uri, layoutParams);
        }finally {
            parcel.recycle();
        }
    }
}
