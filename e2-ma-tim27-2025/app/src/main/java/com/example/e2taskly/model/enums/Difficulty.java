package com.example.e2taskly.model.enums;

public enum Difficulty {
    EASY(1),
    NORMAL(3),
    HARD(7),
    EPIC(20);

    private final int xpValue;

    Difficulty(int xpValue) {
        this.xpValue = xpValue;
    }

    public int getXpValue() {
        return xpValue;
    }
}