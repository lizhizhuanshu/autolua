package top.lizhistudio.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import top.lizhistudio.autolua.AutoLuaEngine;
import top.lizhistudio.autolua.LuaInterpreter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.test_button);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoLuaEngine autoLuaEngine = MainApplication.getAutoLuaEngine();

                autoLuaEngine.execute(AssetManager.read(MainActivity.this,"testLua.lua"), "test", new LuaInterpreter.Callback() {
                    @Override
                    public void onCompleted(Object[] result) {
                        for (Object o:result)
                        {
                            Log.e("==========","result "+o);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }
        });
    }
}