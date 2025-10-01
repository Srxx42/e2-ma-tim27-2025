package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;

public class MainActivity extends BaseActivity {

    private Button buttonAddTask;
    private ImageView menuButton;
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

        sharedPreferences = new SharedPreferencesUtil(this);
        buttonAddTask = findViewById(R.id.addTask);
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

        //userService.addXpToUser(sharedPreferences.getActiveUserUid(),150);
    }
    @Override
    protected void onResume(){
        super.onResume();
        checkUserActivityStreak();
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
}