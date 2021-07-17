package top.lizhistudio.app.activity;

import android.content.Intent;
import android.os.Bundle;

import top.lizhistudio.app.MainService;

public class PrepareActivity extends PermissionActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCompletedPermission() {
        if (isCompleted)
            return;
        isCompleted = true;
        startService(new Intent(PrepareActivity.this, MainService.class));
        startActivity(new Intent(PrepareActivity.this, MainActivity.class));
    }
}
