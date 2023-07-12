package com.example.maxbuyer_shop_bot.repository;

import com.example.maxbuyer_shop_bot.entity.Product;
import com.example.maxbuyer_shop_bot.entity.Product_User;
import com.example.maxbuyer_shop_bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProductRepository extends JpaRepository<Product_User,Long> {
    List<Product_User> findByUser(User user);



    Optional<Product_User> findByUserAndProduct(User user, Product product);


    @Query("SELECT pu FROM Product_User pu WHERE pu.user = :user AND pu.product = :product")
    Product_User findByUserAndProduct1(@Param("user") User user, @Param("product") Product product);
    List<Product_User> findByProduct(Product productToDelete);
}
