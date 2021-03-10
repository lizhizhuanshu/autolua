package top.lizhistudio.app.javawrapper;

import android.content.Context;
import android.widget.Toast;

import com.immomo.mls.utils.MainThreadExecutor;

import top.lizhistudio.autolua.annotation.RPCMethod;

public interface IToast {

    @RPCMethod
    void show(String message);

    class ToastImplement implements IToast
    {
        private final Context context;
        public ToastImplement(Context context)
        {
            this.context = context;
        }

        @Override
        public void show(String message) {
            MainThreadExecutor.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
