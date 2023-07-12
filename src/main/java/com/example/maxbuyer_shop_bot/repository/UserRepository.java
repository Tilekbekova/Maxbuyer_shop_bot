package com.example.maxbuyer_shop_bot.repository;

import com.example.maxbuyer_shop_bot.entity.Product;
import com.example.maxbuyer_shop_bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
