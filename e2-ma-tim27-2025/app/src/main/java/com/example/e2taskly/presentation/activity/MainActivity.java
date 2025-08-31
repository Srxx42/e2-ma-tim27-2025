package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e2taskly.R;
import com.example.e2taskly.service.UserService;

public class MainActivity extends AppCompatActivity {
    private Button buttonLogout;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        userService = new UserService(this);
        buttonLogout = findViewById(R.id.buttonLogout);

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


    }
    private void handleLogout() {
        userService.logoutUser();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}