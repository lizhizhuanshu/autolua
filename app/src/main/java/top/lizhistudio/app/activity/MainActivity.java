package top.lizhistudio.app.activity;


import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.autolua.core.AutoLuaEngine;
import top.lizhistudio.app.App;
import top.lizhistudio.app.R;
import top.lizhistudio.app.core.ProjectManager;

//TODO 设置端口，控制调试器开关的界面，最好显示当前的ip地址
//TODO debuggerServer 增加一个监听器方法

//TODO 实现VSCode 调试器插件

//TODO 将本地地址转换成相对地址

public class MainActivity extends Activity {
    private long lastBackPressedTime = 0;
    private LinearLayout projectsView;
    private ProjectManager.Observer observer;
    private ProjectManager projectManager;

    @Override
    public void onBackPressed() {
        long time = SystemClock.uptimeMillis();
        if(time - lastBackPressedTime < 3000)
        {
            AutoLuaEngine.getInstance().sendStop();
            finish();
        }else
        {
            Toast.makeText(this,"再次点击将退出软件",Toast.LENGTH_SHORT).show();
            lastBackPressedTime = time;
        }
    }

    private void removeProjectView(String name)
    {
        for (int i=0;i<projectsView.getChildCount();i++)
        {
            View child = projectsView.getChildAt(i);
            TextView projectName = child.findViewById(R.id.projectName);
            if (name.equals(projectName.getText().toString()))
            {
                projectsView.removeViewAt(i);
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        projectsView = findViewById(R.id.projects);
        App.getApp().getFloatControllerView().conceal();
        projectManager = ProjectManagerImplement.getInstance();
        observer = new ProjectManager.Observer() {
            @Override
            public void onUpdate(String projectName, ProjectManager.Event event) {
                if (event == ProjectManager.Event.DELETE)
                    removeProjectView(projectName);
                else
                    addProjectView(projectName);
            }
        };
        projectManager.attach(observer);

        for (String name: ProjectManagerImplement.getInstance().allProjectName())
        {
            addProjectView(name);
        }



        TextView textView = findViewById(R.id.addProject);
        textView.setOnClickListener(new View.OnClickListener() {
            private int  id = 0;
            @Override
            public void onClick(View v) {
                projectManager.createProject("project"+id,"feature"+id,id);
                id++;
            }
        });

    }


    private void addProjectView(String name)
    {
        View rowView = View.inflate(this,R.layout.project,null);
        TextView projectName = rowView.findViewById(R.id.projectName);
        Button startProject = rowView.findViewById(R.id.startProject);
        Button deleteProject = rowView.findViewById(R.id.deleteProject);
        projectName.setText(name);
        startProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getApp().getFloatControllerView().show();
                App.getApp().getFloatControllerView().reShow();
                MainActivity.this.finish();
            }
        });
        deleteProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectManagerImplement.getInstance().deleteProject(name);
            }
        });
        projectsView.addView(rowView);
    }


    @Override
    protected void onPause() {
        super.onPause();
        projectManager.detach(observer);
    }
}
