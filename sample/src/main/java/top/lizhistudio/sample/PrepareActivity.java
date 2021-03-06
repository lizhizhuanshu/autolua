package com.lizhizhuanshu.firstdream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import top.lizhistudio.auxiliary.view.PermissionActivity;
import top.lizhistudio.host.AssetManager;

public class PrepareActivity extends PermissionActivity {
    private static final boolean DEBUG   = false;
    static final int VERSION   = 11;
    private boolean releasing = false;


    private int getLocalVersion()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Record",MODE_PRIVATE);
        return sharedPreferences.getInt("version",0);
    }

    private boolean isNeedReleaseSource()
    {
        return !releasing &&(DEBUG ||getLocalVersion()<VERSION) ;
    }


    private void setLocalVersion(int n)
    {
        SharedPreferences.Editor editor = getSharedPreferences("Record",MODE_PRIVATE).edit();
        editor.apply();
        editor.putInt("version",n);
        editor.commit();
    }

    private synchronized void onCompletedTask()
    {
        isCompleted = true;
        startActivity(new Intent(PrepareActivity.this,MainActivity.class));
    }

    private String getFilePath()
    {
        try{
            return getFilesDir().getCanonicalPath();
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }



//    private void initializeAuxiliary()
//    {
//
//        initialize =1;
//        String appName = getString(R.string.app_name);
//        Starter starter = new Starter(this.getPackageCodePath())
//                .setServiceName("LiZhiStudio-"+appName)
//                .setPackageName(this.getPackageName())
//                .setUIName(MainService.class.getName())
//                .setScriptPath(getFilePath()+"/"+appName+"/?.lua;"
//                        +getFilePath()+"/"+appName+"/library/?.lua;")
//                .setDebugPrint(DEBUG);
//        starter.checkClient(new Starter.Callback() {
//            @Override
//            public void onCompleted(boolean is) {
//                if(is)
//                {
//                    initialize = 2;
//                    onCompletedTask();
//                }else
//                    Toast.makeText(PrepareActivity.this,"脚本环境初始化失败!!!",Toast.LENGTH_LONG).show();
//            }
//        },new Handler(Looper.getMainLooper()));
//
//    }


    private void releaseSource()
    {
        final String appName = getString(R.string.app_name);
        releasing = true;
        Toast.makeText(this,"正在释放源文件,请稍后",Toast.LENGTH_LONG).show();;
        new Thread(){
            @Override
            public void run() {
                try{
                    AssetManager.copyAssetsTo(PrepareActivity.this,
                            appName,
                            getFilePath()+"/"+appName,true);
                    setLocalVersion(VERSION);
                    onCompletedTask();
                }catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                releasing = false;
            }
        }.start();
    }



    @Override
    protected void onCompletedPermission() {
        if (isCompleted)
            return ;
        if(isNeedReleaseSource())
            releaseSource();
        else
            onCompletedTask();

    }
}
