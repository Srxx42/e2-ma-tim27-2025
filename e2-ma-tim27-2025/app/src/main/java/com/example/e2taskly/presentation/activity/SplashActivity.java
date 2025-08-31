package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.e2taskly.R;
import com.example.e2taskly.util.SharedPreferencesUtil;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferencesUtil sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = new SharedPreferencesUtil(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        if (sharedPreferences.getActiveUserUid() != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {

            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}