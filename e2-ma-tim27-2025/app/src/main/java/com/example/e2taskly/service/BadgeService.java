package com.example.e2taskly.service;

import android.content.Context;

import com.example.e2taskly.data.repository.BadgeRepository;
import com.example.e2taskly.model.UserBadge;
import com.example.e2taskly.model.enums.BadgeType;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class BadgeService {
    private final BadgeRepository badgeRepository;

    public BadgeService(Context context){
        badgeRepository = new BadgeRepository(context);
    }

    public Task<UserBadge> createBadge(String userUid, int completedNumber) {
        BadgeType badgeType;

        if (completedNumber == 100) badgeType = BadgeType.DIAMOND;
        else if (completedNumber >= 30) badgeType = BadgeType.GOLD;
        else if (completedNumber >= 20) badgeType = BadgeType.SILVER;
        else badgeType = BadgeType.BRONZE;

        UserBadge badge = new UserBadge("", userUid, badgeType);

        return badgeRepository.createBadge(badge).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return badge;
        });
    }

    public Task<List<UserBadge>> getUserBadges(String userId) {
        return badgeRepository.getUserBadges(userId);
    }


}
