package com.example.e2taskly.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.e2taskly.model.Task;
import com.example.e2taskly.model.enums.Difficulty;
import com.example.e2taskly.model.enums.Importance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class XpCounterManager {

    private static final String PREFS_NAME = "XpCounters";
    private final SharedPreferences sharedPreferences;
    private final String userId;

    public XpCounterManager(Context context, String userId) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.userId = userId;
    }

    /**
     * Glavna metoda koja izračunava koliko XP poena treba dodeliti za dati task.
     * Vraća ukupan zbir XP poena (za Difficulty + za Importance) koji su ispod limita.
     */
    public int calculateXpToAward(Task task) {
        int xpFromDifficulty = 0;
        int xpFromImportance = 0;

        // Provera za Difficulty
        String difficultyKey = getDifficultyCounterKey(task.getDifficulty());
        int currentDifficultyCount = sharedPreferences.getInt(difficultyKey, 0);
        int maxDifficultyCount = getMaxCountForDifficulty(task.getDifficulty());

        if (currentDifficultyCount < maxDifficultyCount) {
            xpFromDifficulty = getXpForDifficulty(task.getDifficulty());
        }

        // Provera za Importance
        String importanceKey = getImportanceCounterKey(task.getImportance());
        int currentImportanceCount = sharedPreferences.getInt(importanceKey, 0);
        int maxImportanceCount = getMaxCountForImportance(task.getImportance());

        if (currentImportanceCount < maxImportanceCount) {
            xpFromImportance = getXpForImportance(task.getImportance());
        }

        return xpFromDifficulty + xpFromImportance;
    }

    /**
     * Beleži da su XP poeni dodeljeni, inkrementirajući odgovarajuće brojače.
     * Poziva se NAKON što je `addXpToUser` uspešno izvršen.
     */
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

    // Vrednosti XP poena
    private int getXpForDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return 1;
            case NORMAL: return 3;
            case HARD: return 7;
            case EPIC: return 20;
            default: return 0;
        }
    }

    private int getXpForImportance(Importance importance) {
        switch (importance) {
            case NORMAL: return 1;
            case IMPORTANT: return 3;
            case URGENT: return 10;
            case SPECIAL: return 100;
            default: return 0;
        }
    }
}