package top.lizhistudio.app.activity;

import android.content.Intent;
import android.os.Bundle;


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
        startActivity(new Intent(PrepareActivity.this, MainActivity.class));
    }
}
