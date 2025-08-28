package com.example.e2taskly.model;

import java.util.Date;

public class User {
    private String uid;
    private  String email;
    private String username;
    private String avatar;
    private int level;
    private int xp;
    private boolean isActivated;
    private Date registrationTime;

    public User() {
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
}
