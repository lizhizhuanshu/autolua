package top.lizhistudio.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import top.lizhistudio.app.App;

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
        App.getApp().getAutoLuaEngineImplement2().detach(engineObserver);
    }

    private static class EngineObserver implements AutoLuaEngineImplement2.Observer
    {
        private Context context;
        private EngineObserver(Context context)
        {
            this.context = context;
        }
        @Override
        public synchronized void onUpdate(AutoLuaEngineImplement2.STATE state) {
            if (state == AutoLuaEngineImplement2.STATE.RUNNING && context != null)
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
        if (App.getApp().getAutoLuaEngineImplement2().getState() == AutoLuaEngineImplement2.STATE.RUNNING)
            startActivity(new Intent(this, MainActivity.class));
        App.getApp().getAutoLuaEngineImplement2().attach(engineObserver);
        App.getApp().getAutoLuaEngineImplement2().start();
    }
}
