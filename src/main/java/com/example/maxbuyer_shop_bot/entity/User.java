package com.example.maxbuyer_shop_bot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    private Long id;

    private String username;
    @Enumerated(EnumType.STRING)
    private Country country;





    // Constructor, getters, and setters






}

