package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.BadgeType;

public class UserBadge {
    private String badgeId;
    private String userUid;
    private BadgeType badgeType;

    public UserBadge(){}

    public UserBadge(String badgeId, String userUid, BadgeType badgeType) {
        this.badgeId = badgeId;
        this.userUid = userUid;
        this.badgeType = badgeType;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public BadgeType getBadgeType() {
        return badgeType;
    }

    public void setBadgeType(BadgeType badgeType) {
        this.badgeType = badgeType;
    }
}
