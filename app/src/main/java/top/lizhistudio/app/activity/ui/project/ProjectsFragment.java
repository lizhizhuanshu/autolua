package top.lizhistudio.app.activity.ui.project;

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

import top.lizhistudio.app.App;
import top.lizhistudio.app.FloatControllerListener;
import top.lizhistudio.app.R;
import top.lizhistudio.app.core.ProjectManager;
import top.lizhistudio.app.core.implement.ProjectManagerImplement;
import top.lizhistudio.app.view.FloatControllerView;

public class ProjectsFragment extends Fragment {
    private LinearLayout projectsView;
    private ProjectManager.Observer observer;
    private ProjectManager projectManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_project, container, false);
        projectsView = root.findViewById(R.id.projects);
        App.getApp().getFloatControllerView().conceal();
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
                FloatControllerView controllerView = App.getApp().getFloatControllerView();
                controllerView.setOnClickListener(new FloatControllerListener(name));
                controllerView.reShow();
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