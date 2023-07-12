package com.example.maxbuyer_shop_bot;

import com.example.maxbuyer_shop_bot.config.MaxBuyerTelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
public class MaxbuyerShopBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaxbuyerShopBotApplication.class, args);

}}
