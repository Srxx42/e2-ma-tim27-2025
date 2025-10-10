package com.example.e2taskly.model;

import com.example.e2taskly.model.enums.BonusType;
import com.example.e2taskly.model.enums.EquipmentType;

public class EquipmentTemplate {
    private String id;
    private String name;
    private String description;
    private EquipmentType type;
    private BonusType bonusType;
    private double bonusValue;
    private int durationInFights;
    private int costPercentage;
    private int upgradeCostPercentage;

    public EquipmentTemplate() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }

    public double getBonusValue() {
        return bonusValue;
    }

    public void setBonusValue(double bonusValue) {
        this.bonusValue = bonusValue;
    }

    public int getDurationInFights() {
        return durationInFights;
    }

    public void setDurationInFights(int durationInFights) {
        this.durationInFights = durationInFights;
    }

    public int getCostPercentage() {
        return costPercentage;
    }

    public void setCostPercentage(int costPercentage) {
        this.costPercentage = costPercentage;
    }

    public int getUpgradeCostPercentage() {
        return upgradeCostPercentage;
    }

    public void setUpgradeCostPercentage(int upgradeCostPercentage) {
        this.upgradeCostPercentage = upgradeCostPercentage;
    }
}
