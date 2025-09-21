package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.e2taskly.R;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.LevelingService;
import com.example.e2taskly.service.UserService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ProfileActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "com.example.e2taskly.USER_ID";
    private ImageView imageViewAvatar, imageViewQrCode;
    private TextView textViewUsername, textViewTitle, textViewLevel, textViewXp, textViewPower, textViewCoins,textViewXpProgress;
    private Button buttonChangePassword;
    private ProgressBar progressBar,progressBarXp;
    private LinearLayout powerLayout,coinsLayout;
    private UserService userService;
    private LevelingService levelingService;
    private String profileUserId;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userService = new UserService(this);
        levelingService = new LevelingService();
        setupViews();

        Intent intent = getIntent();
        profileUserId = intent.getStringExtra(EXTRA_USER_ID);
        currentUserId = userService.getCurrentUserId();
        if(profileUserId==null || profileUserId.isEmpty()){
            profileUserId = currentUserId;
        }
        if(profileUserId==null){
            Toast.makeText(this,"User not found",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        loadUserProfile();
    }
    private void setupViews(){
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewLevel = findViewById(R.id.textViewLevel);
        textViewXp = findViewById(R.id.textViewXp);
        textViewXpProgress = findViewById(R.id.textViewXpProgress);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        progressBar = findViewById(R.id.progressBar);
        progressBarXp = findViewById(R.id.progressBarXp);

        powerLayout = findViewById(R.id.powerLayout);
        coinsLayout = findViewById(R.id.coinsLayout);
        textViewPower = findViewById(R.id.textViewPower);
        textViewCoins = findViewById(R.id.textViewCoins);
    }
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        userService.getUserProfile(profileUserId)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        populateUI(task.getResult());
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to load profile: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void populateUI(User user) {
        if (user == null) return;

        textViewUsername.setText(user.getUsername());
        textViewTitle.setText(user.getTitle());
        textViewLevel.setText(String.valueOf(user.getLevel()));
        textViewXp.setText(String.valueOf(user.getXp()));

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            int avatarResId = getResources().getIdentifier(user.getAvatar(), "drawable", getPackageName());
            if (avatarResId != 0) {
                imageViewAvatar.setImageResource(avatarResId);
            }
        }

        generateAndSetQrCode(user.getUid());

        boolean isOwner = profileUserId.equals(currentUserId);
        if(isOwner){
            powerLayout.setVisibility(View.VISIBLE);
            coinsLayout.setVisibility(View.VISIBLE);
            buttonChangePassword.setVisibility(View.VISIBLE);
            textViewPower.setText(String.valueOf(user.getPowerPoints()));
            textViewCoins.setText(String.valueOf(user.getCoins()));
        }else{
            powerLayout.setVisibility(View.GONE);
            coinsLayout.setVisibility(View.GONE);
            buttonChangePassword.setVisibility(View.GONE);
        }
        displayProgress(user);
    }
    private void generateAndSetQrCode(String uid){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(uid, BarcodeFormat.QR_CODE, 400, 400);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showChangePasswordDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        final EditText etOldPassword = dialogView.findViewById(R.id.editTextOldPassword);
        final EditText etNewPassword = dialogView.findViewById(R.id.editTextNewPassword);
        final EditText etConfirmPassword = dialogView.findViewById(R.id.editTextConfirmPassword);

        builder.setView(dialogView) // Postavlja vaš XML kao sadržaj
                .setTitle(R.string.profile_change_password_dialog_title)

                .setPositiveButton(R.string.dialog_change_button, (dialog, id) -> {
                    String oldPass = etOldPassword.getText().toString();
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    performPasswordChange(oldPass, newPass, confirmPass);
                })

                .setNegativeButton(R.string.dialog_cancel_button, (dialog, id) -> {
                    dialog.cancel();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void performPasswordChange(String oldPass, String newPass, String confirmPass) {
        progressBar.setVisibility(View.VISIBLE);
        userService.changePassword(oldPass, newPass, confirmPass)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to change password: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void displayProgress(User user) {
        int currentLevel = user.getLevel();
        int currentXp = user.getXp();

        int xpForCurrentLevel = (currentLevel > 1) ? levelingService.getXpForLevel(currentLevel) : 0;


        int xpForNextLevel = levelingService.getXpForLevel(currentLevel + 1);

        int xpProgressInLevel = currentXp - xpForCurrentLevel;
        int xpNeededInLevel = xpForNextLevel - xpForCurrentLevel;

        textViewXpProgress.setText(xpProgressInLevel + " / " + xpNeededInLevel + " XP");

        if (xpNeededInLevel > 0) {
            progressBarXp.setMax(xpNeededInLevel);
            progressBarXp.setProgress(xpProgressInLevel);
        }
    }
}