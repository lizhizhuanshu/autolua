package top.lizhistudio.sample;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;

import top.lizhistudio.androidlua.JavaObjectWrapperFactory;
import top.lizhistudio.androidlua.JavaObjectWrapperFactoryImplement;
import top.lizhistudio.autolua.AutoLuaEngine;
import top.lizhistudio.autolua.LazyAutoLuaEngine;
import top.lizhistudio.autolua.LocalBuilder;
import top.lizhistudio.autolua.LuaInterpreter;
import top.lizhistudio.autolua.LuaInterpreterImplement;


public class MainApplication  extends Application {
    private static AutoLuaEngine autoLuaEngine;
    @Override
    public void onCreate() {
        super.onCreate();
        JavaObjectWrapperFactoryImplement.Builder builder = new JavaObjectWrapperFactoryImplement.Builder();
        JavaObjectWrapperFactory javaObjectWrapperFactory = builder.build();
        autoLuaEngine = new MyAutoLuaEngine(new LuaInterpreterImplement(javaObjectWrapperFactory),this).proxy();
    }

    public static AutoLuaEngine getAutoLuaEngine()
    {
        return autoLuaEngine;
    }

    private static class MyAutoLuaEngine extends LazyAutoLuaEngine
    {
        private Context context;
        private Handler handler;
        public MyAutoLuaEngine(LuaInterpreter luaInterpreter, Context context)
        {
            super(luaInterpreter);
            this.context = context;
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected void onHint(String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                }
            });
        }

        private String getRootPath()
        {
            try{
                return context.getFilesDir().getCanonicalPath();
            }catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }

        private String getScriptLoadPath()
        {
            String rootPath = getRootPath() +"/"+context.getString(R.string.app_name)+"/";
            return rootPath + "?.lua;"+rootPath + "library/?.lua";
        }

        @Override
        protected top.lizhistudio.autolua.AutoLuaEngine create(LuaInterpreter interpreter) {
            LocalBuilder builder = new LocalBuilder(context,interpreter);
            builder.setDebugPrint(true)
                    .setUse64Bit()
                    .setScriptPath(getScriptLoadPath());
            return builder.build();
        }
    }
}
