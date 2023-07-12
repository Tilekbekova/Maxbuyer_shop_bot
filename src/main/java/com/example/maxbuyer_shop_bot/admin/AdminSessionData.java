package com.example.maxbuyer_shop_bot.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class AdminSessionData {
    private int currentStep;
    private Map<String, Object> productData;

    public AdminSessionData() {
        currentStep =0;
        productData = new HashMap<>();
    }

    // Геттеры и сеттеры для currentStep и productData
}
