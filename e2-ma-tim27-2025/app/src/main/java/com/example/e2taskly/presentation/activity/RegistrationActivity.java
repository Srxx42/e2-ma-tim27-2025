package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
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
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {
    private final List<ImageView> avatarImageViews = new ArrayList<>();
    private String selectedAvatarId = null;
    private TextInputLayout textInputLayoutEmail, textInputLayoutUsername, textInputLayoutPassword, textInputLayoutConfirmPassword;
    private TextInputEditText editTextEmail, editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLogin;


    private UserService userService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        userService = new UserService(this);

        setContentView(R.layout.activity_registration);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);

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
        setupInputValidationListeners();
    }
    private void attemptRegistration() {
        if (!isFormValid()) {
            return;
        }

        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
        String username = editTextUsername.getText() != null ? editTextUsername.getText().toString().trim() : "";
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : "";
        String confirmPassword = editTextConfirmPassword.getText() != null ? editTextConfirmPassword.getText().toString().trim() : "";

        progressBar.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        userService.registerUser(email, username, password, confirmPassword, selectedAvatarId)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this,
                                "Registration successful! A verification link has been sent to your email.",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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

    private void setupInputValidationListeners() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    int id = focusedView.getId();
                    if (id == R.id.editTextEmail) {
                        validateEmail();
                    } else if (id == R.id.editTextUsername) {
                        validateUsername();
                    } else if (id == R.id.editTextPassword) {
                        validatePassword();
                    } else if (id == R.id.editTextConfirmPassword) {
                        validateConfirmPassword();
                    }
                }
            }
        };

        editTextEmail.addTextChangedListener(validationWatcher);
        editTextUsername.addTextChangedListener(validationWatcher);
        editTextPassword.addTextChangedListener(validationWatcher);
        editTextConfirmPassword.addTextChangedListener(validationWatcher);
    }

    private boolean validateEmail() {
        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setError("Email is required.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError("Please enter a valid email address.");
            return false;
        } else {
            textInputLayoutEmail.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String username = editTextUsername.getText() != null ? editTextUsername.getText().toString().trim() : "";
        String usernamePattern = "^[a-zA-Z0-9_]{3,20}$";
        if (TextUtils.isEmpty(username)) {
            textInputLayoutUsername.setError("Username is required.");
            return false;
        } else if (!username.matches(usernamePattern)) {
            textInputLayoutUsername.setError("Username must be 3-20 characters long (letters, numbers, _).");
            return false;
        } else {
            textInputLayoutUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : "";
        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setError("Password is required.");
            return false;
        } else if (password.length() < 6) {
            textInputLayoutPassword.setError("Password must be at least 6 characters long.");
            return false;
        } else {
            textInputLayoutPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : "";
        String confirmPassword = editTextConfirmPassword.getText() != null ? editTextConfirmPassword.getText().toString().trim() : "";
        if (!password.equals(confirmPassword)) {
            textInputLayoutConfirmPassword.setError("Passwords do not match.");
            return false;
        } else {
            textInputLayoutConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean validateAvatar() {
        if (selectedAvatarId == null || selectedAvatarId.isEmpty()) {
            Toast.makeText(this, "Please select an avatar.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isFormValid() {
        boolean isEmailValid = validateEmail();
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();
        boolean isConfirmPasswordValid = validateConfirmPassword();
        boolean isAvatarSelected = validateAvatar();

        return isEmailValid && isUsernameValid && isPasswordValid && isConfirmPasswordValid && isAvatarSelected;
    }
    private void setupAvatarSelection() {
        ImageView imageViewAvatar1 = findViewById(R.id.imageViewAvatar1);
        ImageView imageViewAvatar2 = findViewById(R.id.imageViewAvatar2);
        ImageView imageViewAvatar3 = findViewById(R.id.imageViewAvatar3);
        ImageView imageViewAvatar4 = findViewById(R.id.imageViewAvatar4);
        ImageView imageViewAvatar5 = findViewById(R.id.imageViewAvatar5);

        avatarImageViews.add(imageViewAvatar1);
        avatarImageViews.add(imageViewAvatar2);
        avatarImageViews.add(imageViewAvatar3);
        avatarImageViews.add(imageViewAvatar4);
        avatarImageViews.add(imageViewAvatar5);

        View.OnClickListener avatarClickListener = v -> selectAvatar((ImageView) v);

        imageViewAvatar1.setOnClickListener(avatarClickListener);
        imageViewAvatar2.setOnClickListener(avatarClickListener);
        imageViewAvatar3.setOnClickListener(avatarClickListener);
        imageViewAvatar4.setOnClickListener(avatarClickListener);
        imageViewAvatar5.setOnClickListener(avatarClickListener);
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