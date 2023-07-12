package com.example.maxbuyer_shop_bot.entity;

public enum Subcategory {
    MEN("Мужина"),
    WOMEN("Женищина");
    private String value;

    Subcategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Subcategory fromValue(String value) {
        for (Subcategory subcategory : Subcategory.values()) {
            if (subcategory.getValue().equalsIgnoreCase(value)) {
                return subcategory;
            }
        }
        throw new IllegalArgumentException("Invalid Category value: " + value);
    }
}

