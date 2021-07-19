package top.lizhistudio.app;

import android.app.Application;
import android.util.Log;

import com.immomo.mls.MLSBuilder;
import com.immomo.mls.MLSEngine;


import com.immomo.mls.fun.lt.SIApplication;

import org.luaj.vm2.Globals;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.androidlua.NotReleaseLuaFunctionAdapter;
import top.lizhistudio.app.core.ProjectManagerImplement;
import top.lizhistudio.app.core.UserInterfaceImplement;
import top.lizhistudio.app.core.UserdataUI;
import top.lizhistudio.app.provider.GlideImageProvider;
import top.lizhistudio.app.util.AssetManager;
import top.lizhistudio.app.view.FloatControllerView;
import top.lizhistudio.app.view.FloatControllerViewImplement;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.RemoteLuaContextManager;


public class App extends Application {
    private static App app;
    private FloatControllerView floatControllerView;
    private AutoLuaEngine autoLuaEngine;
    private volatile String rootPath = null;
    private void initializeAutoLuaEngine()
    {
        RemoteLuaContextManager remoteLuaContextManager = new RemoteLuaContextManager.Builder()
                .outputPrintListener(new RemoteLuaContextManager.PrintListener() {
                    @Override
                    public void onPrint(String message) {
                        Log.d("AutoLuaEngine",message);
                    }
                })
                .errorPrintListener(new RemoteLuaContextManager.PrintListener() {
                    @Override
                    public void onPrint(String message) {
                        Log.e("AutoLuaEngine",message);
                    }
                })
                .build(this);
        autoLuaEngine = new AutoLuaEngine(remoteLuaContextManager);
        autoLuaEngine.addInitializeMethod(new NotReleaseLuaFunctionAdapter() {
            @Override
            public int onExecute(LuaContext luaContext) throws Throwable {
                byte[] code = AssetManager.read(App.this,"initialize.lua");
                luaContext.loadBuffer(code,"initialize", LuaContext.CODE_TYPE.TEXT);
                luaContext.pCall(0,0,0);
                String rootPath = App.this.rootPath;
                if (rootPath != null)
                {
                    luaContext.push(rootPath);
                    luaContext.setGlobal("ROOT_PATH");
                    luaContext.getGlobal("package");
                    luaContext.push("path");
                    luaContext.push(rootPath + "/?.lua;");
                    luaContext.setTable(-3);
                    luaContext.pop(1);
                }
                return 0;
            }
        });
        autoLuaEngine.addInitializeMethod(UserInterfaceImplement.getDefault().getRegistrar());
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

    public void setRootPath(String path)
    {
        rootPath = path;
    }


}




