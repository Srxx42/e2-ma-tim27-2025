package com.example.e2taskly.service;

import java.util.Arrays;
import java.util.List;

public class LevelingService {
    private static final List<String> TITLES  = Arrays.asList(
            "Rookie", "Apprentice", "Journeyman", "Adept", "Expert", "Master", "Grandmaster"
    );
    public int getXpForLevel(int level){
        if(level<=2){
            return 200;
        }
        int previousLevelXp = getXpForLevel(level-1);
        double requiredXp = previousLevelXp * 2 + (double) previousLevelXp / 2;
        return (int) (Math.ceil(requiredXp / 100.0)) * 100;

    }
    public int getPowerPointsForLevel(int level){
        if(level<2){
            return 0;
        }
        if(level==2){
            return 40;
        }
        int previousLevelPp = getPowerPointsForLevel(level-1);
        double newPp = previousLevelPp + (3.0 / 4.0) * previousLevelPp;
        return (int) Math.round(newPp);
    }
    public String getTitleForLevel(int level){
        if(level - 1 < TITLES.size()){
            return TITLES.get(level-1);
        }
        return TITLES.get(TITLES.size()-1);
    }
    public int calculateNextXpGain(int baseXp, int level) {
        double xp = baseXp;
        for (int i = 1; i < level; i++) {
            xp = xp + xp / 2.0;
        }
        return (int) Math.round(xp);
    }
    public int getCoinsRewardForLevel(int level) {
        if (level <= 1) {
            return 200;
        }
        int previousReward = getCoinsRewardForLevel(level - 1);
        return (int) Math.round(previousReward * 1.20);
    }
}
