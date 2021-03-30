package top.lizhistudio.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.immomo.mls.MLSBuilder;
import com.immomo.mls.MLSEngine;


import com.immomo.mls.fun.lt.SIApplication;

import org.luaj.vm2.Globals;

import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.app.core.implement.UserInterfaceImplement;
import top.lizhistudio.app.core.implement.UserdataUI;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.app.provider.GlideImageProvider;
import top.lizhistudio.app.view.FloatControllerView;
import top.lizhistudio.app.view.FloatControllerViewImplement;
import top.lizhistudio.autolua.core.BaseLuaContextFactory;
import top.lizhistudio.autolua.core.BasePrintListener;
import top.lizhistudio.autolua.core.UserInterface;


public class App extends Application {
    private static App app;
    private FloatControllerView floatControllerView;
    private AutoLuaEngine autoLuaEngine;

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
        autoLuaEngine = new AutoLuaEngine();
        autoLuaEngine.getStartConfig()
                .setProcessPrint(true)
                .setPackagePath(this.getPackageCodePath());
        autoLuaEngine.attach(new EngineObserver());
        autoLuaEngine.register(BaseLuaContextFactory.AUTO_LUA_UI_NAME,
                UserInterface.class,
                UserInterfaceImplement.getDefault());
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
        UserInterfaceImplement.getDefault().initialize(this);
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

    public AutoLuaEngine getAutoLuaEngine()
    {
        return autoLuaEngine;
    }
}




