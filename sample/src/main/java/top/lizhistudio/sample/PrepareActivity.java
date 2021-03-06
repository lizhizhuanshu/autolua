package top.lizhistudio.sample;

import android.content.Intent;

public class PrepareActivity extends PermissionActivity {
    @Override
    protected void onCompletedPermission() {
        startActivity(new Intent(this,MainActivity.class));
    }
}
