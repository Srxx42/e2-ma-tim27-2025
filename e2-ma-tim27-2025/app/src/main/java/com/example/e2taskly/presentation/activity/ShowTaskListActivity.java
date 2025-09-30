package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.e2taskly.R;
import com.example.e2taskly.adapters.CategoryListAdapter;
import com.example.e2taskly.adapters.TaskListAdapter;
import com.example.e2taskly.model.RepeatingTask;
import com.example.e2taskly.model.SingleTask;
import com.example.e2taskly.model.enums.TaskType;
import com.example.e2taskly.service.TaskCategoryService;
import com.example.e2taskly.service.TaskService;
import com.example.e2taskly.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShowTaskListActivity extends AppCompatActivity {

    private TaskService taskService;

    private UserService userService;

    private List<SingleTask> singleTasks;

    private List<RepeatingTask> repeatingTasks;

    private ListView  singleTaskListView;

    private ListView repeatingTaskListView;

    private Button btnSingleShow, btnRepeatingShow;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        taskService = new TaskService(this);
        userService = new UserService(this);

        setContentView(R.layout.activity_task_list_show);

        Toolbar toolbar =findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        initViews();
        initListeners();

        updateListViewVisibility(1);


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalDate today = LocalDate.now();


        singleTasks = taskService.getAllSingleTasks();
        singleTasks.removeIf(s -> s.getTaskDate().isBefore(today));

        repeatingTasks = taskService.getAllRepeatingTasks();
        repeatingTasks.removeIf(r -> r.getFinishingDate().isBefore(today));

        TaskListAdapter sAdapter = new TaskListAdapter(this, new ArrayList<>(singleTasks),true);
        singleTaskListView.setAdapter(sAdapter);

        TaskListAdapter rAdapter = new TaskListAdapter(this, new ArrayList<>(repeatingTasks),true);
        repeatingTaskListView.setAdapter(rAdapter);
    }

    private void initViews(){
        btnSingleShow = findViewById(R.id.btnSingleShow);
        btnRepeatingShow = findViewById(R.id.btnRepeatingShow);

        singleTaskListView = findViewById(R.id.singleTaskListView);
        repeatingTaskListView = findViewById(R.id.repeatingTaskListView);

        LocalDate today = LocalDate.now();

        singleTasks = new ArrayList<>();
        singleTasks = taskService.getAllSingleTasks();

        singleTasks.removeIf(s -> s.getTaskDate().isBefore(today));

        repeatingTasks = new ArrayList<>();
        repeatingTasks = taskService.getAllRepeatingTasks();

        repeatingTasks.removeIf(r -> r.getFinishingDate().isBefore(today));

        TaskListAdapter sAdapter = new TaskListAdapter(this, new ArrayList<>(singleTasks),true);
        singleTaskListView.setAdapter(sAdapter);

        TaskListAdapter rAdapter = new TaskListAdapter(this, new ArrayList<>(repeatingTasks),true);
        repeatingTaskListView.setAdapter(rAdapter);
    }

    private void initListeners(){
        btnSingleShow.setOnClickListener(v -> {
            updateListViewVisibility(1);
        });

        btnRepeatingShow.setOnClickListener(v ->{
            updateListViewVisibility(2);
        });
    }

    private void updateListViewVisibility(int id){
        if(id == 1){
            singleTaskListView.setVisibility(View.VISIBLE);
            repeatingTaskListView.setVisibility(View.GONE);
        } else {
            singleTaskListView.setVisibility(View.GONE);
            repeatingTaskListView.setVisibility(View.VISIBLE);
        }
    }



}
