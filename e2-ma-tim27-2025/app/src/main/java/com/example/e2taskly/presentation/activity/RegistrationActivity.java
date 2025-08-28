package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {
    private List<ImageView> avatarImageViews = new ArrayList<>();
    private String selectedAvatarId = null;
    private TextInputEditText editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLogin;

    // Deklaracija servisnog sloja
    private UserService userService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        userService = new UserService(this);

        setContentView(R.layout.activity_registration);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.textViewLogin);
        buttonRegister.setOnClickListener(v -> attemptRegistration());

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        setupAvatarSelection();

    }
    private void attemptRegistration() {
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        userService.registerUser(email, username, password, confirmPassword, selectedAvatarId, task -> {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(RegistrationActivity.this,
                        "Registration successful! A verification link has been sent to your email.",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                String errorMessage = "An unknown error has occurred.";
                if (task.getException() != null) {
                    errorMessage = task.getException().getMessage();
                }
                Toast.makeText(RegistrationActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setupAvatarSelection() {
        ImageView imageViewAvatar1 = findViewById(R.id.imageViewAvatar1);
        ImageView imageViewAvatar2 = findViewById(R.id.imageViewAvatar2);
        ImageView imageViewAvatar3 = findViewById(R.id.imageViewAvatar3);

        avatarImageViews.add(imageViewAvatar1);
        avatarImageViews.add(imageViewAvatar2);
        avatarImageViews.add(imageViewAvatar3);

        View.OnClickListener avatarClickListener = v -> selectAvatar((ImageView) v);

        imageViewAvatar1.setOnClickListener(avatarClickListener);
        imageViewAvatar2.setOnClickListener(avatarClickListener);
        imageViewAvatar3.setOnClickListener(avatarClickListener);
    }

    private void selectAvatar(ImageView selectedImageView) {
        for (ImageView imageView : avatarImageViews) {
            if (imageView == selectedImageView) {
                imageView.setBackgroundResource(R.drawable.avatar_border_selected);
                selectedAvatarId = (String) imageView.getTag();
            } else {
                imageView.setBackgroundResource(R.drawable.avatar_border_unselected);
            }
        }
        Log.d("AvatarSelection", "Selected: " + selectedAvatarId);
    }
}