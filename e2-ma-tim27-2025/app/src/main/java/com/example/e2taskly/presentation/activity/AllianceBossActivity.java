package com.example.e2taskly.presentation.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.e2taskly.R;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.model.EquipmentTemplate;
import com.example.e2taskly.model.User;
import com.example.e2taskly.service.AllianceService;
import com.example.e2taskly.service.BossService;
import com.example.e2taskly.service.EquipmentService;
import com.example.e2taskly.service.MissionProgressService;
import com.example.e2taskly.service.UserService;
import com.google.android.gms.tasks.Tasks;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class AllianceBossActivity extends AppCompatActivity {

    // Services
    private UserService userService;
    private AllianceService allianceService;
    private BossService bossService;

    private EquipmentService equipmentService;
    private MissionProgressService missionProgressService;

    // Data models
    private User currentUser;
    private Alliance currentAlliance;
    private Boss currentBoss;

    // UI Elements
    private RelativeLayout allianceBossUiContainer, rewardOverlayContainer;
    private LinearLayout allianceNoMissionMessage, llRewardsContainer, llRewardItems;
    private ProgressBar allianceBossHpBar;
    private TextView tvAllianceBossHp, tvDaysLeft, tvRewardGoldAmount, tvMissionStatusTitle;
    private ImageView ivAllianceBoss, ivAllianceChestGif, ivRewardItem1, ivRewardItem2;
    private Button btnStartMission, btnShowProgress;

    private boolean isChestOpen = false;
    private int goldToGet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_boss);

        // Inicijalizacija servisa
        userService = new UserService(this);
        allianceService = new AllianceService(this);
        bossService = new BossService(this);
        missionProgressService = new MissionProgressService(this);
        equipmentService = new EquipmentService(this);

        initializeUI();
        loadInitialData();
    }

    private void initializeUI() {
        allianceBossUiContainer = findViewById(R.id.alliance_boss_ui_container);
        rewardOverlayContainer = findViewById(R.id.reward_overlay_container);
        allianceNoMissionMessage = findViewById(R.id.alliance_no_mission_message);
        llRewardsContainer = findViewById(R.id.ll_alliance_rewards_container);
        llRewardItems = findViewById(R.id.ll_reward_items);
        allianceBossHpBar = findViewById(R.id.alliance_boss_hp_bar);
        tvAllianceBossHp = findViewById(R.id.tv_alliance_boss_hp);
        tvDaysLeft = findViewById(R.id.tv_days_left);
        tvRewardGoldAmount = findViewById(R.id.tv_reward_gold_amount);
        tvMissionStatusTitle = findViewById(R.id.tv_mission_status_title);
        ivAllianceBoss = findViewById(R.id.iv_alliance_boss);
        ivAllianceChestGif = findViewById(R.id.iv_alliance_chest_gif);
        ivRewardItem1 = findViewById(R.id.iv_reward_item1);
        ivRewardItem2 = findViewById(R.id.iv_reward_item2);
        btnStartMission = findViewById(R.id.btn_start_mission);
        btnShowProgress = findViewById(R.id.btn_show_progress);
    }

    private void loadInitialData() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Započinjemo lanac asinhronih poziva
        userService.getUserProfile(currentUserId)
                .onSuccessTask(user -> {
                    this.currentUser = user;
                    return bossService.getByEnemyId(user.getUid(),false);
                }).onSuccessTask(userBoss -> {
                    if (userBoss == null) {
                        this.goldToGet = 50;
                    } else {
                        this.goldToGet = (int) ((userBoss.getBossGold() * 1.2) / 2);
                    }
                    return allianceService.getAlliance(currentUser.getAllianceId());
                })
                .onSuccessTask(alliance -> {
                    this.currentAlliance = alliance;
                    return bossService.getByEnemyId(alliance.getAllianceId(), true);
                })
                .addOnSuccessListener(boss -> {
                    this.currentBoss = boss; // Može biti null ako misija ne postoji
                    updateUiBasedOnState();
                    equipmentService.loadAllTemplates()
                            .addOnSuccessListener(aVoid -> {

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to load game data. Please try again.", Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading alliance data.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUiBasedOnState() {
        if (currentBoss == null) {
            setupNoMissionState(true);
        }
        LocalDate appearanceDate = currentBoss.getBossAppearanceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long daysPassed = ChronoUnit.DAYS.between(appearanceDate, LocalDate.now());
        boolean isMissionExpired = daysPassed >= 14;

        boolean wasMissionSuccessful = currentBoss.isBossBeaten();

        if (wasMissionSuccessful) {
            setupMissionCompleteState();
        } else if (isMissionExpired) {
            setupNoMissionState(false);
        } else {
            setupMissionActiveState();
        }
    }

    private void setupNoMissionState(boolean isInitialState) {
        allianceBossUiContainer.setVisibility(GONE);
        allianceNoMissionMessage.setVisibility(VISIBLE);
        rewardOverlayContainer.setVisibility(GONE);
        btnStartMission.setVisibility(GONE);

        if (isInitialState) {
            tvMissionStatusTitle.setText("SPECIAL MISSION");
            tvMissionStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tvMissionStatusTitle.setText("YOU FAILED SUPER MISSION");
            tvMissionStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        if (currentUser.getUid().equals(currentAlliance.getLeaderId())) {
            btnStartMission.setVisibility(VISIBLE);
            btnStartMission.setOnClickListener(v -> startNewMission());
        }
    }

    @SuppressLint("DefaultLocale")
    private void setupMissionActiveState() {
        allianceNoMissionMessage.setVisibility(View.GONE);
        allianceBossUiContainer.setVisibility(View.VISIBLE);
        rewardOverlayContainer.setVisibility(View.GONE);
        btnStartMission.setVisibility(GONE);

        ivAllianceBoss.setVisibility(VISIBLE);
        allianceBossHpBar.setVisibility(VISIBLE);
        tvAllianceBossHp.setVisibility(VISIBLE);
        findViewById(R.id.ll_countdown_container).setVisibility(VISIBLE);

        // Postavljanje HP bara
        float maxHp = currentAlliance.getMemberIds().size() * 100; // Pretpostavka za max HP
        float currentHp = currentBoss.getBossHp();
        int progress = (int) ((currentHp / maxHp) * 100);
        allianceBossHpBar.setProgress(progress);
        tvAllianceBossHp.setText(String.format("%.0f / %.0f", currentHp, maxHp));

        // Postavljanje slike bossa na osnovu procenta HP-a
        if (progress <= 25) {
            ivAllianceBoss.setImageResource(R.drawable.alliance_boss_25);
        } else if (progress <= 50) {
            ivAllianceBoss.setImageResource(R.drawable.alliance_boss_50);
        } else if (progress <= 75) {
            ivAllianceBoss.setImageResource(R.drawable.alliance_boss_75);
        } else {
            ivAllianceBoss.setImageResource(R.drawable.alliance_boss_100);
        }

        // Izračunavanje preostalih dana
        LocalDate appearanceDate = currentBoss.getBossAppearanceDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate expiryDate = appearanceDate.plusDays(14);
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        tvDaysLeft.setText(String.valueOf(daysLeft > 0 ? daysLeft : 0));

        btnShowProgress.setOnClickListener(v -> {
             Intent intent = new Intent(AllianceBossActivity.this, AllianceBossProgressActivity.class);
             startActivity(intent);
        });
    }

    private void setupMissionCompleteState() {
        allianceBossUiContainer.setVisibility(GONE);
        allianceNoMissionMessage.setVisibility(VISIBLE);
        rewardOverlayContainer.setVisibility(GONE);
        btnStartMission.setVisibility(GONE);

        tvMissionStatusTitle.setText("YOU SUCCESSFULLY DEFEATED BOSS");
        tvMissionStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));

        if (currentUser.getUid().equals(currentAlliance.getLeaderId())) {
            btnStartMission.setVisibility(VISIBLE);
            btnStartMission.setOnClickListener(v -> startNewMission());
        }
        // Proveravamo da li je korisnik već pokupio nagradu
        missionProgressService.getUserProgress(currentUser.getUid())
                .addOnSuccessListener(progress -> {
                    if (progress != null && !progress.isDidUserGetReward()) {
                        showRewardScreen();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Could not check rewards status.", Toast.LENGTH_SHORT).show());
    }


    private void startNewMission() {
        btnStartMission.setEnabled(false); // Sprečavamo duple klikove

        bossService.createBoss(currentAlliance.getAllianceId(), true, currentAlliance.getMemberIds().size())
                .onSuccessTask(aVoid -> {
                    // Nakon kreiranja bossa, moramo ga ponovo dohvatiti da bismo dobili njegov ID
                    return bossService.getByEnemyId(currentAlliance.getAllianceId(), true);
                })
                .onSuccessTask(boss -> {
                    this.currentBoss = boss;
                    return missionProgressService.createProgressesForAlliance(currentAlliance.getMemberIds(), currentAlliance.getAllianceId(), boss.getBossId());

                })
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Special Mission Started!", Toast.LENGTH_SHORT).show();
                    updateUiBasedOnState(); // Osvežavamo UI da prikaže novog bossa
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to start mission.", Toast.LENGTH_SHORT).show();
                    btnStartMission.setEnabled(true);
                });
    }

    private void showRewardScreen() {
        isChestOpen = false;
        rewardOverlayContainer.setVisibility(View.VISIBLE);
        llRewardsContainer.setVisibility(View.INVISIBLE);

        Glide.with(this).asGif().load(R.drawable.chest_closed).into(ivAllianceChestGif);
        ivAllianceChestGif.setOnClickListener(v -> openChest());
    }

    private void openChest() {
        if (isChestOpen) return;
        isChestOpen = true;
        ivAllianceChestGif.setClickable(false);

        Glide.with(this).asGif().load(R.drawable.chest_opened)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) { return false; }
                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        resource.setLoopCount(1);
                        return false;
                    }
                }).into(ivAllianceChestGif);

        String currenUserId = userService.getCurrentUserId();

        tvRewardGoldAmount.setText(String.valueOf(goldToGet));
        userService.addCoinsToUser(currenUserId,goldToGet);

        EquipmentTemplate dropedItem1 = equipmentService.getRandomItem(true,false);
        userService.getUserProfile(currenUserId).addOnSuccessListener(currentUser ->{
            equipmentService.handleItemDrop(currentUser, dropedItem1);
        });

        EquipmentTemplate dropedItem2 = equipmentService.getRandomItem(true,true);
        userService.getUserProfile(currenUserId).addOnSuccessListener(currentUser ->{
            equipmentService.handleItemDrop(currentUser, dropedItem2);
        });

        ivRewardItem1.setVisibility(VISIBLE);
        ivRewardItem2.setVisibility(VISIBLE);
        setRewardItemIcon(dropedItem1);
        ivRewardItem2.setImageResource(R.drawable.ic_potion);


        // Ažuriramo progress da označi da je nagrada pokupljena
        missionProgressService.getUserProgress(currentUser.getUid())
                .onSuccessTask(progress -> {
                    if (progress != null) {
                        progress.setDidUserGetReward(true);
                         return missionProgressService.updateProgress(progress);
                    }
                    return Tasks.forResult(null); // Privremeno
                });

        // Prikazujemo nagrade sa malim zakašnjenjem i fade-in animacijom
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            llRewardsContainer.setVisibility(View.VISIBLE);
            llRewardsContainer.startAnimation(fadeIn);
        }, 1200);
    }

    public void setRewardItemIcon(EquipmentTemplate dropedItem){

        switch (dropedItem.getId()) {
            case "clothing_boots_1":
                ivRewardItem1.setImageResource(R.drawable.ic_boots);
                break;
            case "clothing_gloves_1":
                ivRewardItem1.setImageResource(R.drawable.ic_gloves);
                break;
        }

    }
}