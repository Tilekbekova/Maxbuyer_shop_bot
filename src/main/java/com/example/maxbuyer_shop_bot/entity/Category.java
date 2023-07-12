package com.example.maxbuyer_shop_bot.entity;

public enum Category {
    BRUSHORTS_SKIRTS("Брюки/шорты/юбки"),
    DRESSES("Платья"),
    SHIRTS_TOPS("Рубашки/топы"),
    OUTERWEAR("Верхняя одежда");

    private String value;

    Category(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Category fromValue(String value) {
        for (Category category : Category.values()) {
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid Category value: " + value);
    }
}

