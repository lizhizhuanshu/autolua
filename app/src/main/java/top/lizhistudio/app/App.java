package top.lizhistudio.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.immomo.mls.MLSBuilder;
import com.immomo.mls.MLSEngine;
import com.immomo.mls.global.LVConfig;
import com.immomo.mls.global.LVConfigBuilder;


import android.os.Environment;

import com.immomo.mls.fun.lt.SIApplication;
import com.immomo.mls.global.LuaViewConfig;

import org.luaj.vm2.Globals;

import top.lizhistudio.app.core.UI;
import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.app.core.implement.UIImplement;
import top.lizhistudio.app.core.implement.UserdataUI;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.app.core.implement.LuaInterpreterFactoryImplement;
import top.lizhistudio.app.provider.GlideImageProvider;
import top.lizhistudio.app.view.FloatControllerView;
import top.lizhistudio.app.view.FloatControllerViewImplement;


public class App extends Application {
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
        autoLuaEngine.register(UI.SERVICE_NAME,UI.class,UIImplement.getInstance());

        autoLuaEngine.attach(new EngineObserver());
    }

    private String getScriptPath()
    {
        return this.getFilesDir().getPath()+"/"+"projects";
    }


    private void initializeMLSEngine()
    {
        /// -----------配合 Application 使用------------
        SIApplication.isColdBoot = true;
        registerActivityLifecycleCallbacks(new ActivityLifecycleMonitor());
        /// ---------------------END-------------------

        MLSEngine.init(this, false)//BuildConfig.DEBUG)
                .setImageProvider(new GlideImageProvider())             //设置图片加载器，若不设置，则不能显示图片
                .setDefaultLazyLoadImage(false)
                .registerSingleInsance(new MLSBuilder.SIHolder(UserdataUI.LUA_CLASS_NAME,UserdataUI.class))
                .build(true);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        initializeMLSEngine();
        initializeAutoLuaEngine();
        floatControllerView = new FloatControllerViewImplement(this,40);
        UIImplement.getInstance().initialize(this);
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


    private static void log(String s) {
        Log.d("app", s);
    }


    public FloatControllerView getFloatControllerView()
    {
        return floatControllerView;
    }


}




