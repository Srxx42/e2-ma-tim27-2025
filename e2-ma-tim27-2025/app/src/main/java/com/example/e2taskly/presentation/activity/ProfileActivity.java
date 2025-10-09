package com.example.e2taskly.presentation.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.UserInventoryItem;
import com.example.e2taskly.service.EquipmentService;
import com.example.e2taskly.service.LevelingService;
import com.example.e2taskly.service.UserService;
import com.example.e2taskly.util.SharedPreferencesUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {
    public static final String EXTRA_USER_ID = "com.example.e2taskly.USER_ID";
    private ImageView imageViewAvatar, imageViewQrCode;
    private TextView textViewUsername, textViewTitle, textViewLevel, textViewXp, textViewPower, textViewCoins,textViewXpProgress;
    private Button buttonAddFriend, buttonRemoveFriend;
    private Button buttonChangePassword;
    private ProgressBar progressBar,progressBarXp;
    private LinearLayout powerLayout,coinsLayout;
    private UserService userService;
    private LevelingService levelingService;
    private String profileUserId;
    private String currentUserId;
    private User currentUserObject;
    private User profileUserObject;
    private SharedPreferencesUtil sharedPreferences;
    private ImageView menuButton;
    private TextView labelEquipment;
    private LinearLayout layoutEquipment;
    private Button buttonMyEquipment;
    private EquipmentService equipmentService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sharedPreferences = new SharedPreferencesUtil(this);
        setContentView(R.layout.activity_profile);

        setupToolbar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userService = new UserService(this);
        levelingService = new LevelingService();
        equipmentService = new EquipmentService(this);
        setupViews();

        Intent intent = getIntent();
        profileUserId = intent.getStringExtra(EXTRA_USER_ID);
        currentUserId = sharedPreferences.getActiveUserUid();
        if(profileUserId==null || profileUserId.isEmpty()){
            profileUserId = currentUserId;
        }
        if(profileUserId==null){
            Toast.makeText(this,"User not found",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }
    private void setupViews(){
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewLevel = findViewById(R.id.textViewLevel);
        textViewXp = findViewById(R.id.textViewXp);
        textViewXpProgress = findViewById(R.id.textViewXpProgress);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);
        buttonRemoveFriend = findViewById(R.id.buttonRemoveFriend);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        progressBar = findViewById(R.id.progressBar);
        progressBarXp = findViewById(R.id.progressBarXp);
        menuButton = findViewById(R.id.menuButton);

        powerLayout = findViewById(R.id.powerLayout);
        coinsLayout = findViewById(R.id.coinsLayout);
        textViewPower = findViewById(R.id.textViewPower);
        textViewCoins = findViewById(R.id.textViewCoins);
        labelEquipment = findViewById(R.id.labelEquipment);
        layoutEquipment = findViewById(R.id.layoutEquipment);
        buttonMyEquipment = findViewById(R.id.buttonMyEquipment);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
    @Override
    protected int getMenuResourceId() {
        return R.menu.profile_menu;
    }

    @Override
    protected boolean handleMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_statistics) {
            Intent intent = new Intent(ProfileActivity.this, StatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_my_friends) {
            Intent intent = new Intent(ProfileActivity.this, FriendsListActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_logout) {
            userService.logoutUser();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if(itemId == R.id.action_shop){
            Intent intent = new Intent(ProfileActivity.this, ShopActivity.class);
            startActivity(intent);
        }
        return false;
    }
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        // First, load the current user's profile to know the friendship status
        userService.getUserProfile(currentUserId).addOnCompleteListener(currentUserTask -> {
            if (currentUserTask.isSuccessful() && currentUserTask.getResult() != null) {
                this.currentUserObject = currentUserTask.getResult();

                // Now, load the profile of the user being viewed
                userService.getUserProfile(profileUserId).addOnCompleteListener(profileUserTask -> {
                    progressBar.setVisibility(View.GONE);
                    if (profileUserTask.isSuccessful() && profileUserTask.getResult() != null) {
                        this.profileUserObject = profileUserTask.getResult();
                        populateUI(this.profileUserObject);
                    } else {
                        Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading your session.", Toast.LENGTH_LONG).show();
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
            menuButton.setVisibility(View.VISIBLE);
            textViewPower.setText(String.valueOf(user.getPowerPoints()));
            textViewCoins.setText(String.valueOf(user.getCoins()));
            buttonAddFriend.setVisibility(View.GONE);
            buttonRemoveFriend.setVisibility(View.GONE);
            loadAndDisplayAllEquipment(currentUserId);
            buttonMyEquipment.setVisibility(View.VISIBLE);
            buttonMyEquipment.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EquipmentActivity.class);
                startActivity(intent);
            });
        }else{
            powerLayout.setVisibility(View.GONE);
            coinsLayout.setVisibility(View.GONE);
            buttonChangePassword.setVisibility(View.GONE);
            menuButton.setVisibility(View.GONE);
            loadAndDisplayActiveEquipment(user.getUid());
            buttonMyEquipment.setVisibility(View.GONE);
            updateFriendshipButtons();
        }
        displayProgress(user);
    }
    private void updateFriendshipButtons() {
        boolean areFriends = currentUserObject.getFriendIds().contains(profileUserObject.getUid());

        if (areFriends) {
            buttonAddFriend.setVisibility(View.GONE);
            buttonRemoveFriend.setVisibility(View.VISIBLE);
        } else {
            buttonAddFriend.setVisibility(View.VISIBLE);
            buttonRemoveFriend.setVisibility(View.GONE);
        }

        buttonAddFriend.setOnClickListener(v -> {
            buttonAddFriend.setEnabled(false);
            userService.addFriend(currentUserObject.getUid(),profileUserObject.getUid()).addOnCompleteListener(task -> {
                buttonAddFriend.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show();
                    currentUserObject.getFriendIds().add(profileUserObject.getUid()); // Update local state
                    updateFriendshipButtons();
                } else {
                    Toast.makeText(this, "Error adding friend.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonRemoveFriend.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove " + profileUserObject.getUsername() + "?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Remove", (dialog, which) -> {
                        buttonRemoveFriend.setEnabled(false);
                        userService.removeFriend(currentUserObject.getUid(),profileUserObject.getUid()).addOnCompleteListener(task -> {
                            buttonRemoveFriend.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Friend removed.", Toast.LENGTH_SHORT).show();
                                currentUserObject.getFriendIds().remove(profileUserObject.getUid()); // Update local state
                                updateFriendshipButtons();
                            } else {
                                Toast.makeText(this, "Error removing friend.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .show();
        });
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
    private View createEquipmentItemView(UserInventoryItem item, EquipmentTemplate template) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_equipment, layoutEquipment, false);

        ImageView imageView = itemView.findViewById(R.id.imageViewEquipment);
        TextView textViewName = itemView.findViewById(R.id.textViewEquipmentName);
        TextView textViewDescription = itemView.findViewById(R.id.textViewEquipmentDescription);
        TextView textViewStatus = itemView.findViewById(R.id.textViewStatus);

        itemView.findViewById(R.id.buttonActivate).setVisibility(View.GONE);
        textViewName.setText(template.getName());
        textViewDescription.setText(template.getDescription());

        // Prikaz statusa sa bojama
        textViewStatus.setVisibility(View.VISIBLE);
        if (item.isActivated()) {
            textViewStatus.setTextColor(Color.parseColor("#4CAF50")); // Zelena
            textViewStatus.setText(template.getDurationInFights() > 0 ?
                    "Active for " + item.getFightsRemaining() + " more battles" : "Active");
        } else {
            textViewStatus.setTextColor(Color.GRAY);
            textViewStatus.setText("Inactive");
        }

        // Postavljanje slike
        String itemId = template.getId();
        if (itemId.contains("boots")) imageView.setImageResource(R.drawable.ic_boots);
        else if (itemId.contains("gloves")) imageView.setImageResource(R.drawable.ic_gloves);
        else if (itemId.contains("shield")) imageView.setImageResource(R.drawable.ic_shield);
        else if (itemId.contains("potion")) imageView.setImageResource(R.drawable.ic_potion);
        else if (itemId.contains("bow")) imageView.setImageResource(R.drawable.ic_bow);
        else if (itemId.contains("sword")) imageView.setImageResource(R.drawable.ic_sword);

        return itemView;
    }
    private void loadAndDisplayActiveEquipment(String userId) {
        loadEquipment(userId, true);
    }
    private void loadAndDisplayAllEquipment(String userId) {
        loadEquipment(userId, false);
    }
    private void loadEquipment(String userId, boolean onlyActive) {
        layoutEquipment.removeAllViews();

        Task<List<UserInventoryItem>> inventoryTask = equipmentService.getUserInventory(userId);
        Task<List<EquipmentTemplate>> templatesTask = equipmentService.getEquipmentTemplates();

        Tasks.whenAllSuccess(inventoryTask, templatesTask).addOnSuccessListener(results -> {
            List<UserInventoryItem> inventory = (List<UserInventoryItem>) results.get(0);
            List<EquipmentTemplate> templates = (List<EquipmentTemplate>) results.get(1);
            Map<String, EquipmentTemplate> templateMap = new HashMap<>();
            for (EquipmentTemplate t : templates) {
                templateMap.put(t.getId(), t);
            }

            List<UserInventoryItem> itemsToDisplay = new ArrayList<>();
            for (UserInventoryItem item : inventory) {
                if (!onlyActive || item.isActivated()) {
                    itemsToDisplay.add(item);
                }
            }

            if (itemsToDisplay.isEmpty()) {
                labelEquipment.setVisibility(View.VISIBLE);
                layoutEquipment.setVisibility(View.GONE);
                labelEquipment.setText(onlyActive ? "No Active Equipment" : "No Equipment in Inventory");
            } else {
                labelEquipment.setVisibility(View.VISIBLE);
                layoutEquipment.setVisibility(View.VISIBLE);
                labelEquipment.setText(onlyActive ? "Active Equipment" : "My Equipment");

                for (UserInventoryItem item : itemsToDisplay) {
                    EquipmentTemplate template = templateMap.get(item.getTemplateId());
                    if (template != null) {
                        View itemView = createEquipmentItemView(item, template);
                        layoutEquipment.addView(itemView);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            labelEquipment.setVisibility(View.GONE);
            layoutEquipment.setVisibility(View.GONE);
        });
    }


}