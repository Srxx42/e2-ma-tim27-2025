package com.example.e2taskly.model;

public class ActiveBonuses {
    private double ppMultiplier = 1.0;
    private double successChanceBonus = 0.0;
    private double extraAttackChance = 0.0;
    private double coinGainBonusPercent = 0.0;
    public double getPpMultiplier() {
        return ppMultiplier;
    }

    public void setPpMultiplier(double ppMultiplier) {
        this.ppMultiplier = ppMultiplier;
    }

    public double getSuccessChanceBonus() {
        return successChanceBonus;
    }

    public void setSuccessChanceBonus(double successChanceBonus) {
        this.successChanceBonus = successChanceBonus;
    }

    public double getExtraAttackChance() {
        return extraAttackChance;
    }

    public void setExtraAttackChance(double extraAttackChance) {
        this.extraAttackChance = extraAttackChance;
    }
    public double getCoinGainBonusPercent() {
        return coinGainBonusPercent;
    }

    public void setCoinGainBonusPercent(double coinGainBonusPercent) {
        this.coinGainBonusPercent = coinGainBonusPercent;
    }
}
