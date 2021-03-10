package top.lizhistudio.autoluaapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.immomo.mls.MLSEngine;
import com.immomo.mls.global.LVConfigBuilder;


import android.os.Environment;

import com.immomo.mls.fun.lt.SIApplication;

import org.luaj.vm2.Globals;

import top.lizhistudio.autoluaapp.core.implement.ProjectManagerImplement;
import top.lizhistudio.autoluaapp.service.MainService;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autoluaapp.core.implement.LuaInterpreterFactoryImplement;
import top.lizhistudio.autoluaapp.provider.GlideImageProvider;
import top.lizhistudio.autoluaapp.view.FloatControllerView;
import top.lizhistudio.autoluaapp.view.FloatControllerViewImplement;


public class App extends Application {
    public String SD_CARD_PATH;
    private static App app;
    private FloatControllerView floatControllerView;

    private static class EngineObserver implements AutoLuaEngine.Observer
    {
        @Override
        public void onUpdate(AutoLuaEngine.STATE state) {
            if (state == AutoLuaEngine.STATE.RUNNING)
                getApp().startService(new Intent(getApp(), MainService.class));
            else if(state == AutoLuaEngine.STATE.STOP)
                getApp().stopService(new Intent(getApp(),MainService.class));
        }
    }


    private void initializeAutoLuaEngine()
    {
        AutoLuaEngine autoLuaEngine = AutoLuaEngine.getInstance();
        autoLuaEngine.getStartConfig()
                .setProcessPrint(true)
                .setPackagePath(this.getPackageCodePath())
                .setLuaInterpreterFactory(LuaInterpreterFactoryImplement.class);
        autoLuaEngine.attach(new EngineObserver());
    }

    private void initializeMLSEngine()
    {
        /// -----------配合 Application 使用------------
        SIApplication.isColdBoot = true;
        registerActivityLifecycleCallbacks(new ActivityLifecycleMonitor());
        /// ---------------------END-------------------

        MLSEngine.init(this, true)//BuildConfig.DEBUG)
                .setLVConfig(new LVConfigBuilder(this)
                        .setRootDir(SD_CARD_PATH)
                        .setCacheDir(SD_CARD_PATH + "cache")
                        .setImageDir(SD_CARD_PATH + "image")
                        .setGlobalResourceDir(SD_CARD_PATH + "g_res")
                        .build())
                .setImageProvider(new GlideImageProvider())             //设置图片加载器，若不设置，则不能显示图片
                .setDefaultLazyLoadImage(false)
                .build(true);
    }

    private void initializeFloatController()
    {
        floatControllerView = new FloatControllerViewImplement(this,40);
        floatControllerView.setOnClickListener(new FloatControllerView.OnClickListener() {
            @Override
            public void onClick(FloatControllerView floatControllerView, int state) {

            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        init();
        initializeMLSEngine();
        initializeAutoLuaEngine();
        initializeFloatController();
        ProjectManagerImplement.getInstance().initialize(this);
        log("onCreate: " + Globals.isInit() + " " + Globals.isIs32bit());
    }

    public static App getApp() {
        return app;
    }

    public static String getPackageNameImpl() {
        String sPackageName = app.getPackageName();
        if (sPackageName.contains(":")) {
            sPackageName = sPackageName.substring(0, sPackageName.lastIndexOf(":"));
        }
        return sPackageName;
    }

    private void init() {
        try {
            SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (!SD_CARD_PATH.endsWith("/")) {
                SD_CARD_PATH += "/";
            }
            SD_CARD_PATH += "AutoLua/";
        } catch (Exception e) {
        }
    }

    private static void log(String s) {
        Log.d("app", s);
    }


    public FloatControllerView getFloatControllerView()
    {
        return floatControllerView;
    }


}




