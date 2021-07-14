package top.lizhistudio.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.immomo.mls.MLSBuilder;
import com.immomo.mls.MLSEngine;


import com.immomo.mls.fun.lt.SIApplication;

import org.luaj.vm2.Globals;

import java.util.Random;

import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.app.core.implement.UserInterfaceImplement;
import top.lizhistudio.app.core.implement.UserdataUI;
import top.lizhistudio.app.provider.GlideImageProvider;
import top.lizhistudio.app.view.FloatControllerView;
import top.lizhistudio.app.view.FloatControllerViewImplement;


public class App extends Application {
    private static App app;
    private FloatControllerView floatControllerView;
    private AutoLuaEngineImplement2 autoLuaEngineImplement2;

    private static class EngineObserver implements AutoLuaEngineImplement2.Observer
    {
        @Override
        public void onUpdate(AutoLuaEngineImplement2.STATE state) {
            if (state == AutoLuaEngineImplement2.STATE.RUNNING)
                getApp().startService(new Intent(getApp(), MainService.class));
            else if(state == AutoLuaEngineImplement2.STATE.STOP)
                getApp().stopService(new Intent(getApp(),MainService.class));
        }
    }

    private static int random(int min,int max)
    {
        Random random = new  Random();
        return random.nextInt(max) % (max-min+1) + min;
    }

    private void initializeAutoLuaEngine()
    {
        autoLuaEngineImplement2 = new AutoLuaEngineImplement2();
        autoLuaEngineImplement2.getStartConfig()
                .setProcessPrint(true)
                .setPackagePath(this.getPackageCodePath());
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            autoLuaEngineImplement2.getStartConfig().setServiceAddress(random(1995,2002));
        }
        autoLuaEngineImplement2.attach(new EngineObserver());
        autoLuaEngineImplement2.register(BaseLuaContextFactory.AUTO_LUA_UI_NAME,
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

    public AutoLuaEngineImplement2 getAutoLuaEngineImplement2()
    {
        return autoLuaEngineImplement2;
    }
}




