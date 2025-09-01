package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {
    private Button buttonLogout;
    private Button buttonProfile;
    private Button buttonViewAllUsers;
    private UserService userService;
    private SharedPreferencesUtil sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        userService = new UserService(this);

        Toolbar toolbar =findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        // Ovo gasi defaultni title (E2Taskly)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        sharedPreferences = new SharedPreferencesUtil(this);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonProfile = findViewById(R.id.buttonProfile);
        buttonViewAllUsers = findViewById(R.id.buttonViewAllUsers);
        buttonLogout.setOnClickListener(v->handleLogout());
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


        buttonProfile.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
            startActivity(intent);
        });
        buttonViewAllUsers.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UsersListActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume(){
        super.onResume();
        checkUserActivityStreak();
    }
    private void handleLogout() {
        userService.logoutUser();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkUserActivityStreak(){
        userService.getUserProfile(sharedPreferences.getActiveUserUid(),task -> {
            if(task.isSuccessful()){
                userService.updateDailyStreak(task.getResult());
            }
        });
    }
}