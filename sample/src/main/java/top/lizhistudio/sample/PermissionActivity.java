package top.lizhistudio.auxiliary.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import top.lizhistudio.auxiliary.R;


public class PermissionActivity extends Activity {
    private long lastBackPressedTime = 0;
    private static final int FLOAT_PERMISSION_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;
    protected boolean isCompleted = false;
    private boolean isHasFloatPermission()
    {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
    }

    private boolean checkFloatViewPermission()
    {
        boolean result = isHasFloatPermission();
        if(!result)
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            Toast.makeText(this,
                    "需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
            startActivityForResult(intent,FLOAT_PERMISSION_REQUEST_CODE);
        }
        return result;
    }



    public boolean checkStoragePermissions() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
        int permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,STORAGE_PERMISSION_REQUEST_CODE);
        }
        return permission == PackageManager.PERMISSION_GRANTED;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(!isHasFloatPermission())
        {
            if(checkStoragePermissions())
                checkFloatViewPermission();
        }
    }




    protected void onCompletedPermission()
    {
        if(isCompleted)
            return;
        isCompleted = true;
    }




    private void askIsOver(final int type)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("你拒绝了权限申请")
                .setMessage("无法为您提供服务,点击确定退出本软件")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(type == FLOAT_PERMISSION_REQUEST_CODE)
                            checkFloatViewPermission();
                        else
                            checkStoragePermissions();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionActivity.this.finish();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grant : grantResults) {  // 判断是否所有的权限都已经授予了
            Log.i("lgq", "申请权限结果====" + grant);
            if (grant != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                break;
            }
        }
        if (requestCode == FLOAT_PERMISSION_REQUEST_CODE) {
            if(isAllGranted)
                onCompletedPermission();
            else
                askIsOver(FLOAT_PERMISSION_REQUEST_CODE);
        }else if(requestCode == STORAGE_PERMISSION_REQUEST_CODE)
        {
            if(isAllGranted)
            {
                if(checkFloatViewPermission())
                    onCompletedPermission();
            }else
                askIsOver(STORAGE_PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isHasFloatPermission())
            onCompletedPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isCompleted)
            finish();
    }

    @Override
    public void onBackPressed() {
        long time = SystemClock.uptimeMillis();
        if(time - lastBackPressedTime < 3000)
        {
            finish();
        }else
        {
            Toast.makeText(this,"再次点击将退出软件",Toast.LENGTH_SHORT).show();
            lastBackPressedTime = time;
        }
    }

}
