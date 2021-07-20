package top.lizhistudio.app.activity.ui.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;


import com.immomo.mls.utils.MainThreadExecutor;

import top.lizhistudio.app.AutoLuaService;
import top.lizhistudio.app.R;
import top.lizhistudio.app.core.ProjectManager;
import top.lizhistudio.app.core.ProjectManagerImplement;

public class ProjectsFragment extends Fragment {
    private LinearLayout projectsView;
    private ProjectManager.Observer observer;
    private ProjectManager projectManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_project, container, false);
        projectsView = root.findViewById(R.id.projects);
        projectManager = ProjectManagerImplement.getInstance();
        observer = new ProjectManager.Observer() {
            @Override
            public void onUpdate(String projectName, ProjectManager.Event event) {
                MainThreadExecutor.post(new Runnable() {
                    @Override
                    public void run() {
                        if (event == ProjectManager.Event.DELETE)
                            removeProjectView(projectName);
                        else
                            addProjectView(projectName);
                    }
                });
            }
        };
        for (String name: ProjectManagerImplement.getInstance().allProjectName())
        {
            addProjectView(name);
        }
        return root;
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


    private void addProjectView(String name)
    {
        View rowView = View.inflate(getContext(),R.layout.project,null);
        TextView projectName = rowView.findViewById(R.id.projectName);
        Button startProject = rowView.findViewById(R.id.startProject);
        Button deleteProject = rowView.findViewById(R.id.deleteProject);
        projectName.setText(name);
        startProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AutoLuaService.class);
                String path = ProjectManagerImplement.getInstance().getProjectPath(name);
                intent.putExtra("projectPath",path);
                getActivity().startService(intent);
                getActivity().finish();
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
    public void onResume() {
        super.onResume();
        projectManager.attach(observer);
    }

    @Override
    public void onPause() {
        super.onPause();
        projectManager.detach(observer);
    }
}