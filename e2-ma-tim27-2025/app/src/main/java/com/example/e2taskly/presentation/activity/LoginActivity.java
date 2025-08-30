package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.e2taskly.R;
import com.example.e2taskly.service.UserService;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewRegister;

    private UserService userService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        userService = new UserService(this);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.textViewRegister);
        buttonLogin.setOnClickListener(v->attemptLogin());

        textViewRegister.setOnClickListener(v-> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

    }
    private void attemptLogin() {
        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : "";

        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        userService.loginUser(email, password, task -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "An unknown error occurred.";
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}