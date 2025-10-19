package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.example.e2taskly.workers.TaskStatusWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    private Button buttonAddTask;
    private ImageView menuButton;

    private Button buttonShowTaskList;

    private Button buttonShowTaskCalendar;

    private Button buttonShowBossFight;
    private UserService userService;
    private SharedPreferencesUtil sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        userService = new UserService(this);

        setupToolbar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        scheduleDailyTaskStatusCheck();

        sharedPreferences = new SharedPreferencesUtil(this);
        buttonAddTask = findViewById(R.id.addTask);
        buttonShowTaskList = findViewById(R.id.showTaskList);
        buttonShowTaskCalendar = findViewById(R.id.showTaskCalendar);
        buttonShowBossFight = findViewById(R.id.showBossFight);

        menuButton = findViewById(R.id.menuButton);
        Button categoryAdd = findViewById(R.id.categoryAdd);

        categoryAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageCategoryActivity.class);
            startActivity(intent);
        });

        Button showCategories = findViewById(R.id.categoryShow);

        showCategories.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowCategoriesActivity.class);
            startActivity(intent);
        });


        buttonAddTask.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this,ManageTaskActivity.class);
            startActivity(intent);
        });

        buttonShowTaskList.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this,ShowTaskListActivity.class);
            startActivity(intent);
        });

        buttonShowTaskCalendar.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this,ShowTaskCalendarActivity.class);
            startActivity(intent);
        });

        buttonShowBossFight.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this,BossBattleActivity.class);
            startActivity(intent);
        });

        //userService.addXpToUser(sharedPreferences.getActiveUserUid(),150);
    }
    @Override
    protected void onResume(){
        super.onResume();
        checkUserActivityStreak();
    }
    @Override
    protected void preparePopupMenu(PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();

    }
    @Override
    protected int getMenuResourceId() {
        return R.menu.main_menu;
    }

    @Override
    protected boolean handleMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
    private void checkUserActivityStreak(){
        String uid = sharedPreferences.getActiveUserUid();
        if (uid == null || uid.isEmpty()) {
            return;
        }

        userService.getUserProfile(uid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userService.updateDailyStreak(task.getResult());
                    } else {
                        Log.e("MainActivity", "Failed to get user profile for streak check.", task.getException());
                    }
                });
    }

    private void scheduleDailyTaskStatusCheck() {
        PeriodicWorkRequest dailyWorkRequest =
                new PeriodicWorkRequest.Builder(TaskStatusWorker.class, 2, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                "DailyTaskStatusCheck", // Jedinstveno ime za ovaj posao
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
        );
    }
}