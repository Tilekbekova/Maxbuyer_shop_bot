package com.example.maxbuyer_shop_bot.entity;

public enum Country {

    KYRGYSTAN("Кыргызстан"),
    UZBEKISTAN("Узбекитстан"),
    RUSSIA("Россия"),
   KAZAKHSTAN("Казакстан");



    private final String displayName;

    Country(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Country fromValue(String value) {
        for (Country category : Country.values()) {
            if (category.getDisplayName().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid Category value: " + value);
    }
}
