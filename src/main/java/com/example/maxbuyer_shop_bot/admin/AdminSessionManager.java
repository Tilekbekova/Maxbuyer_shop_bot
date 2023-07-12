package com.example.maxbuyer_shop_bot.admin;

import com.example.maxbuyer_shop_bot.entity.Category;
import com.example.maxbuyer_shop_bot.entity.Subcategory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Data
@Component
public class AdminSessionManager {
    private Step currentStep;
    private String productName;
    private double productPrice;
    private Category productCategory;

    private Subcategory subcategory;

    private String imageUrl;

    public AdminSessionManager() {
        reset();
    }

    public void reset() {
        currentStep = Step.NONE;
        productName = "";
        productPrice = 0.0;
        productCategory = null;
        subcategory = null;// Set the category to null
        imageUrl = "";
    }

    public enum Step {
        NONE,
        ENTER_PRODUCT_NAME,
        ENTER_PRODUCT_PRICE,
        ENTER_PRODUCT_CATEGORY,
        ENTER_PRODUCT_SUBCATEGORY,// Add the category step
        ENTER_PRODUCT_IMAGE
        // Add more steps for collecting other product details
    }
}
