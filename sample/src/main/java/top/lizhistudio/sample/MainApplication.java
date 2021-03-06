package top.lizhistudio.sample;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.RenderNode;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.MainThread;

import java.util.HashMap;
import java.util.Map;

import top.lizhistudio.androidlua.JavaObjectWrapperFactory;
import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.LuaContextImplement;
import top.lizhistudio.androidlua.exception.LuaError;
import top.lizhistudio.autolua.annotation.RPCMethod;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaContextFactory;
import top.lizhistudio.autolua.core.RPCJavaInterfaceWrapper;
import top.lizhistudio.autolua.core.Server;
import top.lizhistudio.autolua.rpc.Callback;
import top.lizhistudio.autolua.rpc.ClientHandler;


public class MainApplication  extends Application {
    private static MainApplication self;
    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        FloatControllerView floatControllerView = new FloatControllerViewImplement(this,40);
        floatControllerView.setOnClickListener(new FloatControllerView.OnClickListener() {
            @Override
            public void onClick(FloatControllerView floatControllerView, int state) {
                if (state == FloatControllerView.EXECUTEING_STATE)
                {
                    AutoLuaEngine.getInstance().getInterrupt().interrupt();
                }
                else if(state == FloatControllerView.STOPPED_STATE)
                {
                    floatControllerView.setState(FloatControllerView.EXECUTEING_STATE);

                    byte[] code = AssetManager.read(MainApplication.this,"mainTest.lua");
                    AutoLuaEngine.getInstance().getInterrupt().execute(code, "test", new Callback() {
                        @Override
                        public void onCompleted(Object result) {
                            Object[] os = (Object[]) result;
                            floatControllerView.setState(FloatControllerView.STOPPED_STATE);
                        }
                        @Override
                        public void onError(Throwable throwable) {
                            floatControllerView.setState(FloatControllerView.STOPPED_STATE);
                        }
                    });
                }
            }
        });

        AutoLuaEngine engine = AutoLuaEngine.getInstance();
        engine.getStartConfig()
                .setProcessPrint(true)
                .setPackagePath(this.getPackageCodePath())
                .setLuaContextFactory(BaseLuaContextFactory.class);
        engine.attach(new AutoLuaObserver(floatControllerView));
        engine.register("TestShow",ITestShow.class,new TestShow(this));
    }

    public interface ITestShow
    {
        @RPCMethod
        void showMessage(String s);
    }


    public static class TestShow implements ITestShow
    {
        private Handler mainHandler;
        private Context context;
        public TestShow(Context context)
        {
            this.context = context;
            mainHandler = new Handler(Looper.getMainLooper());
        }

        public void showMessage(String s)
        {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,s,Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    public static class BaseLuaContextFactory implements LuaContextFactory
    {
        private JavaObjectWrapperFactory javaObjectWrapperFactory = null;
        @Override
        public LuaContext newInstance() {
            HashMap<String,Class<?>> services;
            ClientHandler clientHandler = Server.getInstance().getClientHandler();
            try{
                services = clientHandler.getServices();
            }catch (InterruptedException e)
            {
                throw new LuaError(e);
            }
            if (javaObjectWrapperFactory == null)
            {
                JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
                for(Map.Entry<String,Class<?>> serviceSet:services.entrySet())
                {
                    builder.register(new RPCJavaInterfaceWrapper(serviceSet.getValue()));
                }
                javaObjectWrapperFactory = builder.build();
            }
            LuaContextImplement luaContextImplement = new LuaContextImplement(javaObjectWrapperFactory);
            for(Map.Entry<String,Class<?>> serviceSet:services.entrySet())
            {
                luaContextImplement.push(serviceSet.getValue(),clientHandler.getService(serviceSet.getKey(),serviceSet.getValue()));
                luaContextImplement.setGlobal(serviceSet.getKey());
            }
            return luaContextImplement;
        }
    }

    static class AutoLuaObserver implements AutoLuaEngine.Observer
    {
        private static final String TAG = "AutoLuaObserver";
        private FloatControllerView floatControllerView;
        public AutoLuaObserver(FloatControllerView floatControllerView)
        {
            this.floatControllerView = floatControllerView;
        }
        @Override
        public void onUpdate(AutoLuaEngine.STATE state) {
            Log.e("TAG","now AutoLuaEngine state "+state );
            if (state == AutoLuaEngine.STATE.RUNNING)
            {
                self.startService(new Intent(self,MainService.class));
                floatControllerView.show();
            }
            else if(state == AutoLuaEngine.STATE.STOP)
            {
                self.stopService(new Intent(self,MainService.class));
                floatControllerView.conceal();
            }

        }
    }
}




