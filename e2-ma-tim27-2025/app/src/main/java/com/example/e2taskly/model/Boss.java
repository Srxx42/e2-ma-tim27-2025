package com.example.e2taskly.model;

import java.time.LocalDate;
import java.util.Date;

public class Boss {
    private String bossId;
    private String enemyId;
    private int bossLevel;
    private float bossHp;
    private float bossGold;
    private boolean isBossBeaten;
    private boolean didUserFightIt;
    private boolean isAllianceBoss;
    private Date bossAppearanceDate;

    public Boss(){}

    public Boss(String bossId, String enemyId, int bossLevel, float bossHp, float bossGold, boolean isBossBeaten,boolean didUserFightIt ,boolean isAllianceBoss, Date bossAppearanceDate) {
        this.bossId = bossId;
        this.enemyId = enemyId;
        this.bossLevel = bossLevel;
        this.bossHp = bossHp;
        this.bossGold = bossGold;
        this.isBossBeaten = isBossBeaten;
        this.didUserFightIt = didUserFightIt;
        this.isAllianceBoss = isAllianceBoss;
        this.bossAppearanceDate = bossAppearanceDate;
    }

    public boolean isDidUserFightIt() {
        return didUserFightIt;
    }

    public void setDidUserFightIt(boolean didUserFightIt) {
        this.didUserFightIt = didUserFightIt;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
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

    public Date getBossAppearanceDate() {
        return bossAppearanceDate;
    }

    public void setBossAppearanceDate(Date bossAppearanceDate) {
        this.bossAppearanceDate = bossAppearanceDate;
    }
}
