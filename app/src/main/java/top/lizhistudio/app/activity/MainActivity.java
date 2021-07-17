package top.lizhistudio.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.UriUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import top.lizhistudio.androidlua.LuaContext;
import top.lizhistudio.app.App;
import top.lizhistudio.app.R;
import top.lizhistudio.app.core.debugger.DebuggerServer;
import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.core.value.LuaValue;

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

        FloatingActionButton floatingActionButton = findViewById(R.id.add_project);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            new Thread(){
                @Override
                public void run() {
                    File file = UriUtils.uri2File(uri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    {
                        String name = file.getName();
                        File newFile = new File(getCacheDir(),name);
                        try{
                            FileOutputStream outputStream = new FileOutputStream(newFile);
                            outputStream.write(UriUtils.uri2Bytes(uri));
                            outputStream.flush();
                            outputStream.close();
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        file = newFile;
                    }
                    ProjectManagerImplement
                            .getInstance()
                            .addProject(file.getAbsolutePath());
                }
            }.start();
        }
    }
    @Override
    public void onBackPressed() {
        long time = SystemClock.uptimeMillis();
        if(time - lastBackPressedTime < 3000)
        {
            DebuggerServer.getInstance().stop();
            finish();
        }else
        {
            Toast.makeText(this,"再次点击将退出软件",Toast.LENGTH_SHORT).show();
            lastBackPressedTime = time;
        }
    }
}