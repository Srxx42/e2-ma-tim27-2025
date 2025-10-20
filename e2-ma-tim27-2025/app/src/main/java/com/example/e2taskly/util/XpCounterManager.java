package com.example.e2taskly.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.User;
import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;
import com.example.e2taskly.service.LevelingService;
import com.example.e2taskly.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.logging.Level;

public class XpCounterManager {

    private static final String PREFS_NAME = "XpCounters";
    private final SharedPreferences sharedPreferences;
    private final LevelingService levelingService;

    private final UserService userService;
    private final String userId;
    private final Context context;
    private User currentUserObject;

    public XpCounterManager(Context context, String userId) {
        this.context = context;
        this.levelingService = new LevelingService();
        this.userService = new UserService(context);
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.userId = userId;
    }

    public void awardXpForTask(Task task, Runnable onCompletionRunnable) {
        userService.getUserProfile(userId)
                .addOnSuccessListener(user -> {
                    this.currentUserObject = user;

                    int xpToAward = calculateXpToAward(task);

                    if (xpToAward > 0) {
                        userService.addXpToUser(task.getCreatorId(), xpToAward);
                        recordXpAward(task);
                        Toast.makeText(context, "Dodeljeno " + xpToAward + " XP poena!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Dostignut je limit za XP za ovu vrstu zadatka.", Toast.LENGTH_LONG).show();
                    }

                    if (onCompletionRunnable != null) {
                        onCompletionRunnable.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Greška pri učitavanju korisničkog profila.", Toast.LENGTH_LONG).show();

                    if (onCompletionRunnable != null) {
                        onCompletionRunnable.run();
                    }
                });
    }


    public int calculateXpToAward(Task task) {
        int xpFromDifficulty = 0;
        int xpFromImportance = 0;

        // Provera za Difficulty
        String difficultyKey = getDifficultyCounterKey(task.getDifficulty());
        int currentDifficultyCount = sharedPreferences.getInt(difficultyKey, 0);
        int maxDifficultyCount = getMaxCountForDifficulty(task.getDifficulty());

        if (currentDifficultyCount < maxDifficultyCount) {
            int baseDifficulty = task.getDifficulty().getXpValue();
            xpFromDifficulty = levelingService.calculateNextXpGain(baseDifficulty,currentUserObject.getLevel());
        }

        // Provera za Importance
        String importanceKey = getImportanceCounterKey(task.getImportance());
        int currentImportanceCount = sharedPreferences.getInt(importanceKey, 0);
        int maxImportanceCount = getMaxCountForImportance(task.getImportance());

        if (currentImportanceCount < maxImportanceCount) {
            int baseImportance = task.getImportance().getXpValue();
            xpFromImportance = levelingService.calculateNextXpGain(baseImportance,currentUserObject.getLevel());
        }

        return xpFromDifficulty + xpFromImportance;
    }

    public void recordXpAward(Task task) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Ažuriraj brojač za Difficulty
        String difficultyKey = getDifficultyCounterKey(task.getDifficulty());
        int currentDifficultyCount = sharedPreferences.getInt(difficultyKey, 0);
        int maxDifficultyCount = getMaxCountForDifficulty(task.getDifficulty());
        if (currentDifficultyCount < maxDifficultyCount) {
            editor.putInt(difficultyKey, currentDifficultyCount + 1);
        }

        // Ažuriraj brojač za Importance
        String importanceKey = getImportanceCounterKey(task.getImportance());
        int currentImportanceCount = sharedPreferences.getInt(importanceKey, 0);
        int maxImportanceCount = getMaxCountForImportance(task.getImportance());
        if (currentImportanceCount < maxImportanceCount) {
            editor.putInt(importanceKey, currentImportanceCount + 1);
        }

        editor.apply();
    }

    // --- Privatne Helper Metode ---

    private String getDifficultyCounterKey(Difficulty difficulty) {
        LocalDate today = LocalDate.now();
        String baseKey = userId + "_DIFFICULTY_" + difficulty.name();
        switch (difficulty) {
            case EASY:
            case NORMAL:
            case HARD:
                return baseKey + "_" + today.format(DateTimeFormatter.ISO_LOCAL_DATE); // Dnevni limit
            case EPIC:
                int weekOfYear = today.get(WeekFields.of(Locale.getDefault()).weekOfYear());
                int year = today.getYear();
                return baseKey + "_" + year + "-W" + weekOfYear; // Nedeljni limit
            default:
                return baseKey;
        }
    }

    private String getImportanceCounterKey(Importance importance) {
        LocalDate today = LocalDate.now();
        String baseKey = userId + "_IMPORTANCE_" + importance.name();
        switch (importance) {
            case NORMAL:
            case IMPORTANT:
            case URGENT:
                return baseKey + "_" + today.format(DateTimeFormatter.ISO_LOCAL_DATE); // Dnevni limit
            case SPECIAL:
                return baseKey + "_" + today.format(DateTimeFormatter.ofPattern("yyyy-MM")); // Mesečni limit
            default:
                return baseKey;
        }
    }

    // Pravila limita
    private int getMaxCountForDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
            case NORMAL:
                return 5;
            case HARD:
                return 2;
            case EPIC:
                return 1;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private int getMaxCountForImportance(Importance importance) {
        switch (importance) {
            case NORMAL:
            case IMPORTANT:
                return 5;
            case URGENT:
                return 2;
            case SPECIAL:
                return 1;
            default:
                return Integer.MAX_VALUE;
        }
    }

}