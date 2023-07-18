package com.example.maxbuyer_shop_bot.user;

import com.example.maxbuyer_shop_bot.admin.AdminSessionManager;
import com.example.maxbuyer_shop_bot.entity.Category;
import com.example.maxbuyer_shop_bot.entity.Country;
import com.example.maxbuyer_shop_bot.entity.Subcategory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Data
@Component
public class UserSessionManager {
    private UserSessionManager.Step currentStep;

    private Category productCategory;

    private Subcategory subcategory;

    private Country country;


    public UserSessionManager() {
        reset();
    }

    public void reset() {
        currentStep = UserSessionManager.Step.NONE;

        productCategory = null;
        country = null;
        subcategory = null;// Set the category to null

    }

    public enum Step {
        NONE,

        ENTER_PRODUCT_CATEGORY,
        ENTER_PRODUCT_SUBCATEGORY,
        ENTER_USER_COUNTRY// Add the category step

        // Add more steps for collecting other product details
    }
}
