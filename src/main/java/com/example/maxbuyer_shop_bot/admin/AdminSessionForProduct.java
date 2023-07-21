package com.example.maxbuyer_shop_bot.admin;

import com.example.maxbuyer_shop_bot.entity.Category;
import com.example.maxbuyer_shop_bot.entity.Subcategory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Data
@Component
public class AdminSessionForProduct {
    private Step currentStep;
    private Category productCategory;

    private Subcategory subcategory;

    public AdminSessionForProduct() {
        reset();
    }

    public void reset() {
        currentStep = Step.NONE;
        productCategory = null;
        subcategory = null;// Set the category to null

    }
    public enum Step {
        NONE,
        PRODUCT_CATEGORY,
        PRODUCT_SUBCATEGORY,// Add the category step

        // Add more steps for collecting other product details
    }
}
