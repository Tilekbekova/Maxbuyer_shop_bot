package com.example.maxbuyer_shop_bot.repository;

import com.example.maxbuyer_shop_bot.entity.Category;
import com.example.maxbuyer_shop_bot.entity.Product;
import com.example.maxbuyer_shop_bot.entity.Product_User;
import com.example.maxbuyer_shop_bot.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> getProductsByCategoryAndSubcategory(Category category, Subcategory subcategory);


}

