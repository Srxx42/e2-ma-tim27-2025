package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e2taskly.R;
import com.example.e2taskly.model.Alliance;
import com.example.e2taskly.model.Boss;
import com.example.e2taskly.model.SpecialMissionProgress;
import com.example.e2taskly.model.User;
import com.example.e2taskly.presentation.adapter.ProgressAdapter;
import com.example.e2taskly.service.AllianceService;
import com.example.e2taskly.service.BossService;
import com.example.e2taskly.service.MissionProgressService;
import com.example.e2taskly.service.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceBossProgressActivity extends AppCompatActivity {

    // Servisi
    private UserService userService;
    private AllianceService allianceService;
    private BossService bossService;
    private MissionProgressService missionProgressService;

    // Podaci
    private User currentUser;
    private Alliance currentAlliance;
    private Boss currentBoss;

    // UI Elementi
    private TextView tvAllianceName;
    private TextView tvTotalEasyTasks, tvTotalHardTasks, tvTotalBossAttacks, tvTotalItemsBought, tvTotalMessages;
    private View yourProgressItem;
    private RecyclerView recyclerViewAllianceProgress;
    private ProgressAdapter progressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_boss_progress);

        initializeServices();
        initializeUI();
        loadDataChain();
    }

    private void initializeServices() {
        userService = new UserService(this);
        allianceService = new AllianceService(this);
        bossService = new BossService(this);
        missionProgressService = new MissionProgressService(this);
    }

    private void initializeUI() {
        tvAllianceName = findViewById(R.id.tv_alliance_name);
        tvTotalEasyTasks = findViewById(R.id.tv_total_easy_tasks);
        tvTotalHardTasks = findViewById(R.id.tv_total_hard_tasks);
        tvTotalBossAttacks = findViewById(R.id.tv_total_boss_attacks);
        tvTotalItemsBought = findViewById(R.id.tv_total_items_bought);
        tvTotalMessages = findViewById(R.id.tv_total_messages);
        yourProgressItem = findViewById(R.id.your_progress_item);
        recyclerViewAllianceProgress = findViewById(R.id.recycler_view_alliance_progress);
        recyclerViewAllianceProgress.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadDataChain() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userService.getUserProfile(currentUserId)
                .continueWithTask(userTask -> {
                    this.currentUser = userTask.getResult();
                    return allianceService.getAlliance(currentUser.getAllianceId());
                })
                .continueWithTask(allianceTask -> {
                    this.currentAlliance = allianceTask.getResult();
                    return bossService.getByEnemyId(currentAlliance.getAllianceId(), true);
                })
                .continueWithTask(bossTask -> {
                    this.currentBoss = bossTask.getResult();
                    if (currentBoss == null) {
                        throw new Exception("No active alliance mission found.");
                    }
                    return missionProgressService.getAllAlianceProgresses(currentAlliance.getAllianceId(), currentBoss.getBossId());
                })
                .continueWithTask(progressListTask -> {
                    List<SpecialMissionProgress> progressList = progressListTask.getResult();
                    List<String> memberIds = new ArrayList<>();
                    for (SpecialMissionProgress progress : progressList) {
                        memberIds.add(progress.getUserUid());
                    }
                    return userService.getUsersByIds(memberIds).continueWith(usersTask -> {
                        Map<String, User> userMap = new HashMap<>();
                        for (User user : usersTask.getResult()) {
                            userMap.put(user.getUid(), user);
                        }
                        return new ProgressDataBundle(progressList, userMap);
                    });
                })
                .addOnSuccessListener(this::populateUiWithData)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading progress: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void populateUiWithData(ProgressDataBundle dataBundle) {
        tvAllianceName.setText(currentAlliance.getName());

        int totalEasy = 0, totalHard = 0, totalAttacks = 0, totalShop = 0, totalMessages = 0;
        SpecialMissionProgress currentUserProgress = null;
        List<SpecialMissionProgress> otherMembersProgress = new ArrayList<>();

        for (SpecialMissionProgress progress : dataBundle.progressList) {
            totalEasy += progress.getEasyTaskCount();
            totalHard += progress.getHardTaskCount();
            totalAttacks += progress.getSuccessfulBossHitCount();
            totalShop += progress.getShoppingCount();
            totalMessages += progress.getMessageCount().size();

            if (progress.getUserUid().equals(currentUser.getUid())) {
                currentUserProgress = progress;
            } else {
                otherMembersProgress.add(progress);
            }
        }

        tvTotalEasyTasks.setText("Easy task: " + totalEasy);
        tvTotalHardTasks.setText("Hard task: " + totalHard);
        tvTotalBossAttacks.setText("Boss attacks: " + totalAttacks);
        tvTotalItemsBought.setText("Items bought: " + totalShop);
        tvTotalMessages.setText("Messages: " + totalMessages);

        if (currentUserProgress != null) {
            bindSingleProgressItem(yourProgressItem, currentUserProgress, currentUser);
        }

        progressAdapter = new ProgressAdapter(this, otherMembersProgress, dataBundle.userMap, currentBoss, missionProgressService);
        recyclerViewAllianceProgress.setAdapter(progressAdapter);
    }

    private void bindSingleProgressItem(View itemView, SpecialMissionProgress progress, User user) {
        ImageView ivAvatar = itemView.findViewById(R.id.iv_user_avatar);
        TextView tvUsername = itemView.findViewById(R.id.tv_username);
        ProgressBar pbProgress = itemView.findViewById(R.id.pb_user_progress);
        TextView tvPercentage = itemView.findViewById(R.id.tv_user_percentage);
        TextView tvEasy = itemView.findViewById(R.id.tv_easy_task_count);
        TextView tvHard = itemView.findViewById(R.id.tv_hard_task_count);
        TextView tvAttacks = itemView.findViewById(R.id.tv_boss_attacks_count);
        TextView tvShop = itemView.findViewById(R.id.tv_items_bought_count);
        TextView tvMessages = itemView.findViewById(R.id.tv_messages_count);
        ImageView ivCheck = itemView.findViewById(R.id.iv_check_progress);

        tvUsername.setText(user.getUsername());
        tvEasy.setText(String.valueOf(progress.getEasyTaskCount()));
        tvHard.setText(String.valueOf(progress.getHardTaskCount()));
        tvAttacks.setText(String.valueOf(progress.getSuccessfulBossHitCount()));
        tvShop.setText(String.valueOf(progress.getShoppingCount()));
        tvMessages.setText(String.valueOf(progress.getMessageCount().size()));

        int resId = getResources().getIdentifier(user.getAvatar(), "drawable", getPackageName());
        if (resId != 0) ivAvatar.setImageResource(resId);

        ivCheck.setVisibility(progress.isCompletedAll() ? View.VISIBLE : View.GONE);

        missionProgressService.calculateUserProgress(user.getUid(), currentBoss.getBossAppearanceDate())
                .addOnSuccessListener(percentage -> {
                    pbProgress.setProgress(percentage);
                    tvPercentage.setText(percentage + "%");
                });
    }

    private static class ProgressDataBundle {
        final List<SpecialMissionProgress> progressList;
        final Map<String, User> userMap;

        ProgressDataBundle(List<SpecialMissionProgress> progressList, Map<String, User> userMap) {
            this.progressList = progressList;
            this.userMap = userMap;
        }
    }
}