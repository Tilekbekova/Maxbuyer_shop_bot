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
}
