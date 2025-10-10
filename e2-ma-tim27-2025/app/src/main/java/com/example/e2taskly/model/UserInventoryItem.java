package com.example.e2taskly.model;

public class UserInventoryItem {
        private String inventoryId;
        private String userId;
        private String templateId;
        private boolean isActivated;
        private int fightsRemaining;
        private double currentBonusValue;
    public UserInventoryItem() {}

    public UserInventoryItem(String inventoryId, String templateId, double baseBonusValue) {
        this.inventoryId = inventoryId;
        this.templateId = templateId;
        this.isActivated = false;
        this.fightsRemaining = 0;
        this.currentBonusValue = baseBonusValue;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public int getFightsRemaining() {
        return fightsRemaining;
    }

    public void setFightsRemaining(int fightsRemaining) {
        this.fightsRemaining = fightsRemaining;
    }

    public double getCurrentBonusValue() {
        return currentBonusValue;
    }

    public void setCurrentBonusValue(double currentBonusValue) {
        this.currentBonusValue = currentBonusValue;
    }
}
