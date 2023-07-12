package com.example.maxbuyer_shop_bot.admin;

// Import necessary packages
import com.example.maxbuyer_shop_bot.entity.Product;
import com.example.maxbuyer_shop_bot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminProductService {

    private final ProductRepository productRepository;

    @Autowired
    public AdminProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addProduct(Product product) {

        productRepository.save(product);
    }
}

