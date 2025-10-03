package com.example.e2taskly.model;

import java.time.LocalDate;

public class Boss {
    private int bossId;
    private String enemyId;
    private int bossLevel;
    private float bossHp;
    private float bossGold;
    private boolean isBossBeaten;
    private boolean isAllianceBoss;
    private LocalDate bossAppearanceDate;

    public Boss(){}

    public Boss(int bossId, String enemyId, int bossLevel, float bossHp, float bossGold, boolean isBossBeaten, boolean isAllianceBoss, LocalDate bossAppearanceDate) {
        this.bossId = bossId;
        this.enemyId = enemyId;
        this.bossLevel = bossLevel;
        this.bossHp = bossHp;
        this.bossGold = bossGold;
        this.isBossBeaten = isBossBeaten;
        this.isAllianceBoss = isAllianceBoss;
        this.bossAppearanceDate = bossAppearanceDate;
    }

    public int getBossId() {
        return bossId;
    }

    public void setBossId(int bossId) {
        this.bossId = bossId;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(String enemyId) {
        this.enemyId = enemyId;
    }

    public int getBossLevel() {
        return bossLevel;
    }

    public void setBossLevel(int bossLevel) {
        this.bossLevel = bossLevel;
    }

    public float getBossHp() {
        return bossHp;
    }

    public void setBossHp(float bossHp) {
        this.bossHp = bossHp;
    }

    public float getBossGold() {
        return bossGold;
    }

    public void setBossGold(float bossGold) {
        this.bossGold = bossGold;
    }

    public boolean isBossBeaten() {
        return isBossBeaten;
    }

    public void setBossBeaten(boolean bossBeaten) {
        isBossBeaten = bossBeaten;
    }

    public boolean isAllianceBoss() {
        return isAllianceBoss;
    }

    public void setAllianceBoss(boolean allianceBoss) {
        isAllianceBoss = allianceBoss;
    }

    public LocalDate getBossAppearanceDate() {
        return bossAppearanceDate;
    }

    public void setBossAppearanceDate(LocalDate bossAppearanceDate) {
        this.bossAppearanceDate = bossAppearanceDate;
    }
}
