package com.example.e2taskly.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private String username;
    private String avatar;
    private int level;
    private int xp;
    private boolean isActivated;
    private Date registrationTime;
    private String title;
    private int powerPoints;
    private int coins;
    private List<String> badges;
    private List<String> equipment;
    private int activeDaysStreak;
    private Date lastActivityDate;
    private List<String> friendIds;
    private String allianceId;
    private String fcmToken;


    public User() {
        this.badges = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.activeDaysStreak=0;
        this.friendIds = new ArrayList<>();
    }

    public User(String uid, String email, String username, String avatar, int level, int xp, boolean isActivated, Date registrationTime) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.level = level;
        this.xp = xp;
        this.isActivated = isActivated;
        this.registrationTime = registrationTime;
    }

    public User(String uid, String email, String username, String avatar, int level, int xp, boolean isActivated, Date registrationTime, String title, int powerPoints, int coins) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.level = level;
        this.xp = xp;
        this.isActivated = isActivated;
        this.registrationTime = registrationTime;
        this.title = title;
        this.powerPoints = powerPoints;
        this.coins = coins;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void setPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public Date getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public int getActiveDaysStreak() {
        return activeDaysStreak;
    }

    public void setActiveDaysStreak(int activeDaysStreak) {
        this.activeDaysStreak = activeDaysStreak;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
