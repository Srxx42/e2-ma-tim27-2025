package com.example.e2taskly.model.enums;

public enum Importance {
    NORMAL(1),
    IMPORTANT(3),
    URGENT(10),
    SPECIAL(100);

    private final int xpValue;

    Importance(int xpValue) {
        this.xpValue = xpValue;
    }

    public int getXpValue() {
        return xpValue;
    }
}