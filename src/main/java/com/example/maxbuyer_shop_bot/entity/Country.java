package com.example.maxbuyer_shop_bot.entity;

public enum Country {

    KYRGYSTAN("Кыргызстан"),
    UZBEKISTAN("Платья"),
    RUSSIA("Рубашки/топы"),
   KAZAKHSTAN("Техника/гаджет");

    private String value;

    Country(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Country fromValue(String value) {
        for (Country category : Country.values()) {
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid Category value: " + value);
    }
}
