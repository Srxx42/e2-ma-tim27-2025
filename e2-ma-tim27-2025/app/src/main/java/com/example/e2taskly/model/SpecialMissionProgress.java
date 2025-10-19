package com.example.e2taskly.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SpecialMissionProgress {

    private String smpId;
    private String userUid;
    private String allianceId;
    private String bossId;
    private int shoppingCount;
    private int easyTaskCount;
    private int hardTaskCount;
    private int successfulBossHitCount;
    private boolean completedAll;
    private boolean didUserGetReward;
    private List<Date> messageCount;

    public SpecialMissionProgress(){
        messageCount = new ArrayList<>();
    }

    public SpecialMissionProgress(String smpId, String userUid, String allianceId, String bossId, int shoppingCount, int easyTaskCount, int hardTaskCount,int successfulBossHitCount ,boolean completedAll, boolean didUserGetReward, List<Date> massageCount) {
        this.smpId = smpId;
        this.userUid = userUid;
        this.allianceId = allianceId;
        this.bossId = bossId;
        this.shoppingCount = shoppingCount;
        this.easyTaskCount = easyTaskCount;
        this.hardTaskCount = hardTaskCount;
        this.successfulBossHitCount = successfulBossHitCount;
        this.completedAll = completedAll;
        this.didUserGetReward = didUserGetReward;
        this.messageCount = massageCount;
    }

    public int getSuccessfulBossHitCount() {
        return successfulBossHitCount;
    }

    public void setSuccessfulBossHitCount(int successfulBossHitCount) {
        this.successfulBossHitCount = successfulBossHitCount;
    }

    public String getSmpId() {
        return smpId;
    }

    public void setSmpId(String smpId) {
        this.smpId = smpId;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public int getShoppingCount() {
        return shoppingCount;
    }

    public void setShoppingCount(int shoppingCount) {
        this.shoppingCount = shoppingCount;
    }

    public int getEasyTaskCount() {
        return easyTaskCount;
    }

    public void setEasyTaskCount(int easyTaskCount) {
        this.easyTaskCount = easyTaskCount;
    }



    public boolean isCompletedAll() {
        return completedAll;
    }

    public void setCompletedAll(boolean completedAll) {
        this.completedAll = completedAll;
    }

    public boolean isDidUserGetReward() {
        return didUserGetReward;
    }

    public void setDidUserGetReward(boolean didUserGetReward) {
        this.didUserGetReward = didUserGetReward;
    }

    public int getHardTaskCount() {
        return hardTaskCount;
    }

    public void setHardTaskCount(int hardTaskCount) {
        this.hardTaskCount = hardTaskCount;
    }

    public List<Date> getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(List<Date> messageCount) {
        this.messageCount = messageCount;
    }
}
