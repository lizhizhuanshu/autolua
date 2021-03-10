package top.lizhistudio.app;

import android.util.Log;
import android.widget.Toast;

import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.app.view.FloatControllerView;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.autolua.core.LuaInterpreter;
import top.lizhistudio.autolua.rpc.Callback;

public class FloatControllerListener implements FloatControllerView.OnClickListener {
    private final String projectName;
    public FloatControllerListener(String projectName)
    {
        this.projectName= projectName;
    }

    @Override
    public void onClick(FloatControllerView floatControllerView, int state) {
        LuaInterpreter luaInterpreter = AutoLuaEngine.getInstance().getInterrupt();
        if (luaInterpreter == null)
        {
            Toast.makeText(App.getApp(),"错误的脚本执行环境，无法执行脚本",Toast.LENGTH_LONG)
                    .show();
        } else if (state == FloatControllerView.EXECUTEING_STATE)
        {
            luaInterpreter.interrupt();
        }else if (state == FloatControllerView.STOPPED_STATE)
        {
            String path = ProjectManagerImplement.getInstance().getProjectPath(projectName);
            if (path == null)
                return;
            luaInterpreter.reset();
            luaInterpreter.setLoadScriptPath(path);
            luaInterpreter.executeFile(path + "/main.lua", new Callback() {
                @Override
                public void onCompleted(Object result) {
                    floatControllerView.setState(FloatControllerView.STOPPED_STATE);
                }
                @Override
                public void onError(Throwable throwable) {
                    if (throwable != null)
                        Log.e("AutoLuaEngine","call error",throwable);
                    floatControllerView.setState(FloatControllerView.STOPPED_STATE);
                }
            });
        }
    }
}
