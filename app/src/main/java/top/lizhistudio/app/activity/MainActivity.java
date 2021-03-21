package top.lizhistudio.app.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import top.lizhistudio.app.App;
import top.lizhistudio.app.R;
import top.lizhistudio.app.core.DebuggerServer;
import top.lizhistudio.autolua.core.AutoLuaEngine;

public class MainActivity extends AppCompatActivity {
    private long lastBackPressedTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_project, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public void onBackPressed() {
        long time = SystemClock.uptimeMillis();
        if(time - lastBackPressedTime < 3000)
        {
            App.getApp().getAutoLuaEngine().sendStop();
            DebuggerServer.getInstance().stop();
            finish();
        }else
        {
            Toast.makeText(this,"再次点击将退出软件",Toast.LENGTH_SHORT).show();
            lastBackPressedTime = time;
        }
    }
}