package top.lizhistudio.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import top.lizhistudio.autolua.core.AutoLuaEngine;

public class PrepareActivity extends PermissionActivity {
    private EngineObserver engineObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engineObserver = new EngineObserver(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        AutoLuaEngine.getInstance().detach(engineObserver);
    }

    private static class EngineObserver implements AutoLuaEngine.Observer
    {
        private Context context;
        private EngineObserver(Context context)
        {
            this.context = context;
        }
        @Override
        public synchronized void onUpdate(AutoLuaEngine.STATE state) {
            if (state == AutoLuaEngine.STATE.RUNNING && context != null)
            {
                Context context = this.context;
                this.context = null;
                context.startActivity(new Intent(context, MainActivity.class));
            }
        }
    }




    @Override
    protected void onCompletedPermission() {
        if (isCompleted)
            return;
        isCompleted = true;
        if (AutoLuaEngine.getInstance().getState() == AutoLuaEngine.STATE.RUNNING)
            startActivity(new Intent(this, MainActivity.class));
        AutoLuaEngine.getInstance().attach(engineObserver);
        AutoLuaEngine.getInstance().start();
    }
}
