package com.example.maxbuyer_shop_bot.config;

import com.example.maxbuyer_shop_bot.admin.AdminProductService;
import com.example.maxbuyer_shop_bot.admin.AdminSessionForProduct;
import com.example.maxbuyer_shop_bot.admin.AdminSessionManager;
import com.example.maxbuyer_shop_bot.entity.*;
import com.example.maxbuyer_shop_bot.entity.User;
import com.example.maxbuyer_shop_bot.repository.ProductRepository;
import com.example.maxbuyer_shop_bot.repository.UserProductRepository;
import com.example.maxbuyer_shop_bot.repository.UserRepository;
import com.example.maxbuyer_shop_bot.user.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

@Slf4j
@Component
public class MaxBuyerTelegramBot extends TelegramLongPollingBot {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProductRepository userProductRepository;

    private static final String BOT_USERNAME = "Maxbuyer_bot";
    @Autowired
    private AdminSessionManager adminSessionManager;

    @Autowired
    private AdminSessionForProduct adminSessionForProduct;
    @Autowired
    private UserSessionManager userSessionManager;
    private static final String BOT_TOKEN = "6339506277:AAEHCDapOS_gOLsHbbW054bDSxLhxjFLUJE";

    private static final String WELCOME_MESSAGE = "Доброе пожаловать в Maxbuyer\uD83E\uDD73!!!\n" +
            "Давайте знакомиться – меня зовут Максим, я являюсь байером в Корее. Занимаюсь выкупом оригинальных товаров из Кореи\uD83C\uDDF0\uD83C\uDDF7  и осуществляю доставку по всему миру\uD83C\uDF0D! \n" +
            "Для того, чтобы смотреть весь ассортимент, нажмите кнопку «Старт»!\uD83E\uDEE1";
    private static final String MAIN_MENU_MESSAGE = "Выберите один из пунктов главного меню:";
    private static final String ADMIN_CHAT_ID1 = "5954381822";
    private static final String ADMIN_CHAT_ID2 = "5727177889";
    private static final String FIRST_QUESTION = "1) ВЫГОДА - чаще всего заплатив мне за мои услуги байера и доставку , Вы все равно сэкономите свои деньги , так как многие товары в Корее дешевле чем в странах СНГ ✅\n" +
            "2) БРАК - Я лично нахожусь в Корее и любой отправляемый товар проходит проверку на качество , после чего отправляется ✅\n" +
            "3) ЭКСКЛЮЗИВ - по мимо того что в наших странах завышенные цены , так еще и очень часто покупатель сталкивается с проблемой выбора , его банально нет. В Корее Вы можете найти одежду и многое другое , чего точно нет ни у кого в вашем городе✅\n" +
            "4) БАЙЕР - от байера зависит очень многое , Я полностью проведу Вас онлайн  -  от выбора товара —> до отправки в ваш город✅";
    private static final String SECOND_QUESTION = "1. Если вас интересует что-то конкретное: 1) отправляете мне фото или название того что вас интересует 2) далее, я нахожу то что вам нужно (отправляю вам Фото или видео с оф магазина или отправляю вам ссылку на оф сайт), если вас устраивает цена в корее, 3) вы отправляете мне деньги 100% от стоимости, Я выкупаю ваш товар и отправляю в ваш город. \n" +
            " Удобно выгодно прозрачно✅ 2. Если вы не отпределились с выбором: \n" +
            "1) вы так же можете обратиться ко мне, если не знаете наверняка чего хотите. я полностью вас проконсультирую и помогу определиться с выбором 2) подберу ваши размеры одежда/обувь, найду для вас любую технику/гаджет \n" +
            "3) проведу вас онлайн -> от выбора, до покупки и отправки в ваш город. На связи 24/7✅";

    private static final String DELIVERY = "Я использую ЕМС доставку - это один из самых надежных и быстрых способов отправить посылку в страны СНГ. \n" +
            "\n" +
            "Сроки доставки от 4-14 дней (в зависимости от страны)";
    private final AdminProductService adminProductService;
    private static final String warnings = "Важно: Для того чтобы обеспечить возможность связи с вами, пожалуйста, укажите ваш Telegram username при оформлении заказа. Ваш username поможет администратору связаться с вами легко и быстро. Если у вас еще нет username, пожалуйста, создайте его в настройках Telegram перед оформлением заказа. Спасибо за сотрудничество!";


    private String chatId;

    @Autowired
    public MaxBuyerTelegramBot(ProductRepository productRepository, AdminProductService adminProductService) {
        this.productRepository = productRepository;
        this.adminProductService = adminProductService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            if (message.hasText()) {
                String messageText = message.getText();
                if (isAdmin(String.valueOf(chatId))) {
                    onAdminUpdateReceived(chatId, messageText, update);
                } else {
                    onUserUpdateReceived(chatId, messageText, message);
                }
            } else if (message.hasPhoto()) {
                handleProductImage(String.valueOf(chatId), message);
            } else {
                sendMessage(String.valueOf(chatId), "Извините, не могу распознать ваш запрос. Пожалуйста, повторите.");
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();
            if (isAdmin(String.valueOf(chatId))) {
                onAdminCallbackQueryReceived(chatId, callbackData, callbackQuery);
            } else {
                onUserCallbackQueryReceived(chatId, callbackData, callbackQuery);
                createBack(String.valueOf(chatId));
            }
        }
    }

    private void onUserUpdateReceived(long chatId, String messageText, Message message) {
        if (messageText.equalsIgnoreCase("/start")) {
            sendStartMessage(chatId);
            updateDB(chatId, message.getFrom().getUserName());
        } else if (messageText.equalsIgnoreCase("/menu")) {
            sendMessage(String.valueOf(chatId), getMainMenu());
        } else if (messageText.equalsIgnoreCase("Часто задаваемые вопросы")) {
            sendFAQ(String.valueOf(chatId));
        } else if (messageText.equalsIgnoreCase("Доставка")) {
            sendMessage(String.valueOf(chatId), DELIVERY);
        } else if (messageText.equalsIgnoreCase("В чем плюсы заказывать одежду/гаджет из Кореи?")) {
            sendMessage(String.valueOf(chatId), FIRST_QUESTION);
            createBack(String.valueOf(chatId));
        } else if (messageText.equalsIgnoreCase("Как заказать?")) {
            sendMessage(String.valueOf(chatId), SECOND_QUESTION);
            createBack(String.valueOf(chatId));
        } else if (messageText.equalsIgnoreCase("Оформить заказ")) {
            sendMessage(String.valueOf(chatId), warnings);
            List<String> countryValues = getCountryValues();
            ReplyKeyboardMarkup c = createKeyboardMarkup(countryValues);
            sendMessage1(String.valueOf(chatId), "Из какой вы страны?", c);

            userSessionManager.setCurrentStep(UserSessionManager.Step.ENTER_USER_COUNTRY);
        } else if (userSessionManager.getCurrentStep() == UserSessionManager.Step.ENTER_USER_COUNTRY) {
            User user = userRepository.findById(chatId).orElseGet(User::new);
            Country country = Country.fromValue(messageText);
            userSessionManager.setCountry(country);
            List<Product_User> productUsers = userProductRepository.findByUser(user);
            if (productUsers.isEmpty()) {
                sendMessage(String.valueOf(chatId), "Корзина пуста.");
            } else {
                sendSelectedProductsToAdmin(String.valueOf(chatId), productUsers, message.getFrom().getUserName(), userSessionManager.getCountry());
                createBack(String.valueOf(chatId));
            }
            userSessionManager.reset();
        } else if (messageText.equalsIgnoreCase("Корзина")) {


            sendProductsInCart(String.valueOf(chatId));
            createBack(String.valueOf(chatId));

        } else if (messageText.equalsIgnoreCase("Другое")) {

            sendSelectedToAdmin(String.valueOf(chatId), message.getFrom().getUserName());
            createBack(String.valueOf(chatId));

        } else if (messageText.equalsIgnoreCase("Каталог")) {
            List<String> categoryValues = getCategoryValues();
            ReplyKeyboardMarkup categoryKeyboardMarkup = createKeyboardMarkup1(categoryValues);
            sendMessage1(String.valueOf(chatId), "Выберите категорию товара:", categoryKeyboardMarkup);
            createBack(String.valueOf(chatId));
            userSessionManager.setCurrentStep(UserSessionManager.Step.ENTER_PRODUCT_CATEGORY);
        } else if (userSessionManager.getCurrentStep() == UserSessionManager.Step.ENTER_PRODUCT_CATEGORY) {
            Category category = Category.fromValue(messageText);
            userSessionManager.setProductCategory(category);
            List<String> subcategoryValues = getSubcategoryValues();
            ReplyKeyboardMarkup subcategoryKeyboardMarkup = createKeyboardMarkup(subcategoryValues);
            sendMessage1(String.valueOf(chatId), "Выберите подкатегорию товара:", subcategoryKeyboardMarkup);
            userSessionManager.setCurrentStep(UserSessionManager.Step.ENTER_PRODUCT_SUBCATEGORY);
        } else if (userSessionManager.getCurrentStep() == UserSessionManager.Step.ENTER_PRODUCT_SUBCATEGORY) {
            Subcategory subcategory = Subcategory.fromValue(messageText);
            userSessionManager.setSubcategory(subcategory);
            sendProductsByCategoryAndSubcategory(String.valueOf(chatId), userSessionManager.getProductCategory(), userSessionManager.getSubcategory());

        } else {
            sendMessage(String.valueOf(chatId), "Извините, не могу распознать ваш запрос. Пожалуйста, повторите.");
        }
    }

    private void onAdminUpdateReceived(long chatId, String messageText, Update update) {

        if (messageText.equalsIgnoreCase("/start")) {
            sendAdminStartMessage(String.valueOf(chatId));
        } else if (messageText.equalsIgnoreCase("/menu")) {
            sendMessage(String.valueOf(chatId), getMainMenu());
        } else if (messageText.equalsIgnoreCase("Добавить товар")) {
            sendMessage(String.valueOf(chatId), "Введите имя товара:");
            adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_NAME);
        } else {
            handleAdminConversation(String.valueOf(chatId), update);

        }
        if (messageText.equalsIgnoreCase("Все товары")) {

            List<String> categoryValues = getCategoryValues();


            ReplyKeyboardMarkup categoryKeyboardMarkup = createKeyboardMarkup(categoryValues);


            sendMessage1(String.valueOf(chatId), "Выберите категорию товара:", categoryKeyboardMarkup);
            adminSessionForProduct.setCurrentStep(AdminSessionForProduct.Step.PRODUCT_CATEGORY);

        } else {
            handleAllProduct(String.valueOf(chatId), update);

        }
    }

    private void onUserCallbackQueryReceived(long chatId, String callbackData, CallbackQuery callbackQuery) {
        if (callbackData.equalsIgnoreCase("Start") || callbackData.equalsIgnoreCase("Back")) {
            sendMainMenuMessage(String.valueOf(chatId));
        } else if (callbackData.startsWith("addToCart_")) {
            String productIdString = callbackData.substring(10);
            long productId = Long.parseLong(productIdString);

            Product productToAdd = productRepository.findById(productId).orElse(null);
            User user = userRepository.findById(chatId).orElseGet(User::new);

            if (userProductRepository.findByUserAndProduct(user, productToAdd).isEmpty()) {
                Product_User product_user = new Product_User();
                product_user.setProduct(productToAdd);
                product_user.setUser(user);
                userProductRepository.save(product_user);
                sendMessage(String.valueOf(chatId), "Товар \"" + productToAdd.getName() + "\" добавлен в корзину.");

            } else {
                sendMessage(String.valueOf(chatId), "Товар уже добавлен в корзину!");
            }


        } else if (callbackData.startsWith("delete_cart")) {

            String productIdString = callbackData.substring(11);
            System.out.println(Long.parseLong(productIdString));
            long productId = Long.parseLong(productIdString);


            Product productToAdd = productRepository.findById(productId).orElse(null);
            User user = userRepository.findById(chatId).orElseGet(User::new);

            if (!userProductRepository.findByUserAndProduct(user, productToAdd).isEmpty()) {

                userProductRepository.delete(userProductRepository.findByUserAndProduct1(user, productToAdd));
                sendMessage(String.valueOf(chatId), "Товар " + productToAdd.getName() + "\\\" удален из корзины!");
            }
        } else {
            sendMessage(String.valueOf(chatId), "Извините, не могу распознать ваш запрос. Пожалуйста, повторите.");
        }
    }

    private void onAdminCallbackQueryReceived(long chatId, String callbackData, CallbackQuery callbackQuery) {
        if (callbackData.equalsIgnoreCase("Start") || callbackData.equalsIgnoreCase("Back")) {
            sendMainMenuMessage(String.valueOf(chatId));
        } else if (isAdmin(String.valueOf(chatId)) && callbackData.startsWith("Delete")) {
            String productIdString = callbackData.substring(6);

            try {
                long productId = Long.parseLong(productIdString);
                Product productToDelete = productRepository.findById(productId).orElse(null);

                if (productToDelete == null) {
                    sendMessage(String.valueOf(chatId), "Нет такого товара!");
                } else {
                    List<Product_User> productUsers = userProductRepository.findByProduct(productToDelete);
                    userProductRepository.deleteAll(productUsers);

                    productRepository.delete(productToDelete);
                    sendMessage(String.valueOf(chatId), "Товар \"" + productToDelete.getName() + "\" удален.");
                }
            } catch (NumberFormatException e) {
                sendMessage(String.valueOf(chatId), "Неверный формат идентификатора товара.");
            }
        } else {
            sendMessage(String.valueOf(chatId), "Извините, не могу распознать ваш запрос. Пожалуйста, повторите.");
        }
    }


    private void updateDB(long userId, String userName) {
        if (userRepository.findById(userId).isEmpty()) {
            User user = new User();
            user.setId(userId);
            user.setUsername(userName);
            userRepository.save(user);
            log.info("Added to DB: " + user);
        }

    }

    private void sendSelectedProductsToAdmin(String chatId, List<Product_User> productUsers, String userName, Country country) {

        if (userName != null && !userName.isEmpty()) {
            // Отправка выбранных продуктов администратору
            StringBuilder message = new StringBuilder("Заказ пользователя " + chatId + ":\n");
            message.append("Страна: ").append(country).append("\n\n");


            for (Product_User productUser : productUsers) {
                Product product = productUser.getProduct();
                message.append("Название: ").append(product.getName()).append("\n");
                message.append("Цена: ").append(product.getPrice()).append("\n\n");

            }


            SendMessage adminMessage = new SendMessage(ADMIN_CHAT_ID1, message.toString());

            // Создайте InlineKeyboardMarkup с кнопкой "Перейти в ЛС"
            InlineKeyboardMarkup keyboardMarkup = createGoToPrivateChatKeyboard(chatId, userName);
            adminMessage.setReplyMarkup(keyboardMarkup);
            sendMessage(chatId, "Заказ отправлен с вами свяжутся");
            try {
                execute(adminMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            String warningMessage = "У вас отсутствует Telegram username. Пожалуйста, создайте его в настройках Telegram и отправьте заявку заново.";
            sendMessage(chatId, warningMessage);
        }

    }

    private void sendSelectedToAdmin(String chatId, String userName) {
        // Отправка выбранных продуктов администратору

        if (userName != null && !userName.isEmpty()) {
            SendMessage adminMessage = new SendMessage(ADMIN_CHAT_ID1, "Заказ пользователя " + chatId + ":\n" + "Другое");

            // Создайте InlineKeyboardMarkup с кнопкой "Перейти в ЛС"
            InlineKeyboardMarkup keyboardMarkup = createGoToPrivateChatKeyboard(chatId, userName);
            adminMessage.setReplyMarkup(keyboardMarkup);
            sendMessage(chatId, "C вами свяжутся");

            try {
                execute(adminMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            String warningMessage = "У вас отсутствует Telegram username. Пожалуйста, создайте его в настройках Telegram и отправьте заявку заново.";
            sendMessage(chatId, warningMessage);
        }

    }


    private InlineKeyboardMarkup createGoToPrivateChatKeyboard(String chatId, String userName) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton goToPrivateChatButton = new InlineKeyboardButton("Перейти в ЛС");

        String privateChatUrl;
        if (userName != null && !userName.isEmpty()) {
            privateChatUrl = "https://t.me/" + userName;
        } else {
            privateChatUrl = "https://t.me/" + chatId;
            goToPrivateChatButton.setText("Перейти в приватный чат по chatId");

        }

        goToPrivateChatButton.setUrl(privateChatUrl);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(goToPrivateChatButton);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }


    private InlineKeyboardMarkup createBack(String chatId) {
        InlineKeyboardMarkup backButtonKeyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
        backButton.setCallbackData("Back");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(backButton);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        backButtonKeyboard.setKeyboard(keyboard);

        // Отправляем сообщение с клавиатурой
        SendMessage message = new SendMessage(String.valueOf(chatId), "Вернуться назад:");
        message.setReplyMarkup(backButtonKeyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return backButtonKeyboard;
    }


    private void handleProductImage(String chatId, Message message) {
        if (adminSessionManager.getCurrentStep() == AdminSessionManager.Step.ENTER_PRODUCT_IMAGE) {
            // Обработка изображения товара
            List<PhotoSize> photoSizes = message.getPhoto();

            PhotoSize largestPhoto = photoSizes.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            if (largestPhoto != null) {
                String fileId = largestPhoto.getFileId();

                GetFile getFileRequest = new GetFile(fileId);
                try {
                    org.telegram.telegrambots.meta.api.objects.File file = execute(getFileRequest);

                    String processedImageBase64 = processImage(file.getFileUrl(getBotToken()));

                    if (processedImageBase64 != null) {
                        // Создание и сохранение объекта товара
                        Product product = new Product();
                        product.setName(adminSessionManager.getProductName());
                        product.setPrice(adminSessionManager.getProductPrice());
                        product.setCategory(adminSessionManager.getProductCategory());
                        product.setSubcategory(adminSessionManager.getSubcategory());
                        product.setImageUrl(processedImageBase64);

                        adminProductService.addProduct(product);


                        sendAdminMenu(String.valueOf(chatId), product.getName());
                        // Сброс административной сессии
                        adminSessionManager.reset();
                    } else {
                        sendMessage(chatId, "Ошибка при обработке изображения. Пожалуйста, повторите попытку.");
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    sendMessage(chatId, "Ошибка при обработке изображения");
                }
            } else {
                sendMessage(chatId, "Изображение не найдено. Пожалуйста, отправьте изображение товара.");
            }
        } else {
            sendMessage(chatId, "Неожиданное изображение товара. Пожалуйста, начните добавление товара с команды \"Добавить товар\".");
        }
    }


    private InlineKeyboardMarkup createStartKeyboardMarkup() {
        ;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton addToCartButton = new InlineKeyboardButton("Старт");
        addToCartButton.setCallbackData("Start");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addToCartButton);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;

    }


    private String processImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            BufferedImage image = ImageIO.read(inputStream);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void handleAdminConversation(String chatId, Update update) {
        String messageText = update.getMessage().getText();
        AdminSessionManager.Step currentStep = adminSessionManager.getCurrentStep();

        switch (currentStep) {
            case ENTER_PRODUCT_NAME:
                // Store the product name and ask for the price
                adminSessionManager.setProductName(messageText);
                sendMessage(chatId, "Введите цену товара:");
                adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_PRICE);
                break;
            case ENTER_PRODUCT_PRICE:
                String price;

                price = messageText;
                adminSessionManager.setProductPrice(price);


                List<String> categoryValues = getCategoryValues();


                ReplyKeyboardMarkup categoryKeyboardMarkup = createKeyboardMarkup(categoryValues);


                sendMessage1(chatId, "Выберите категорию товара:", categoryKeyboardMarkup);
                adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_CATEGORY);

                break;
            case ENTER_PRODUCT_CATEGORY:

                String categoryValue = messageText;

                Category category = Category.fromValue(categoryValue);
                adminSessionManager.setProductCategory(category);


                List<String> subcategoryValues = getSubcategoryValues();


                ReplyKeyboardMarkup subcategoryKeyboardMarkup = createKeyboardMarkup(subcategoryValues);

                sendMessage1(chatId, "Выберите подкатегорию товара:", subcategoryKeyboardMarkup);
                adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_SUBCATEGORY);

                break;
            case ENTER_PRODUCT_SUBCATEGORY:

                String subcategoryValue = messageText;

                Subcategory subcategory = Subcategory.fromValue(subcategoryValue);
                adminSessionManager.setSubcategory(subcategory);

                sendMessage(chatId, "Отправьте картинку товара:");
                adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_IMAGE);

                break;
            case ENTER_PRODUCT_IMAGE:
                // Check if a photo was sent

                if (update.getMessage().hasPhoto()) {
                    // Получение списка размеров фото
                    List<PhotoSize> photoSizes = update.getMessage().getPhoto();

                    // Получение наибольшего размера фото
                    PhotoSize largestPhoto = photoSizes.stream()
                            .max(Comparator.comparing(PhotoSize::getFileSize))
                            .orElse(null);

                    if (largestPhoto != null) {
                        // Получение file ID наибольшего фото
                        String fileId = largestPhoto.getFileId();

                        // Получение файла изображения
                        GetFile getFileRequest = new GetFile(fileId);
                        try {
                            org.telegram.telegrambots.meta.api.objects.File file = execute(getFileRequest);

                            // Обработка файла изображения и сохранение в формате Base64
                            String processedImageBase64 = processImage(file.getFileUrl(getBotToken()));

                            if (processedImageBase64 != null) {
                                // Сохранение Base64-строки в объекте товара
                                Product product = new Product();
                                product.setName(adminSessionManager.getProductName());
                                product.setPrice(adminSessionManager.getProductPrice());
                                product.setCategory(adminSessionManager.getProductCategory());
                                product.setSubcategory(adminSessionManager.getSubcategory());
                                product.setImageUrl(processedImageBase64);

                                // Добавление товара в базу данных
                                adminProductService.addProduct(product);

                                // Отправка сообщения об успешном добавлении товара
                                sendMessage(chatId, "Товар \"" + product.getName() + "\" добавлен в базу данных.");

                                // Сброс административной сессии
                                adminSessionManager.reset();


                            } else {
                                sendMessage(chatId, "Ошибка при обработке изображения. Пожалуйста, повторите попытку.");
                            }
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                            sendMessage(chatId, "Ошибка при обработке изображения");

                        }
                    } else {
                        sendMessage(chatId, "Изображение не найдено. Пожалуйста, отправьте изображение товара.");
                    }
                } else {
                    sendMessage(chatId, "Изображение не найдено. Пожалуйста, отправьте изображение товара.");
                }
                break;

        }
    }

    private void handleAllProduct(String chatId, Update update) {
        String messageText = update.getMessage().getText();
        AdminSessionForProduct.Step currentStep = adminSessionForProduct.getCurrentStep();

        switch (currentStep) {
            case PRODUCT_CATEGORY -> {
                Category category = Category.fromValue(messageText);
                adminSessionForProduct.setProductCategory(category);
                List<String> subcategoryValues = getSubcategoryValues();
                ReplyKeyboardMarkup subcategoryKeyboardMarkup = createKeyboardMarkup(subcategoryValues);
                sendMessage1(chatId, "Выберите подкатегорию товара:", subcategoryKeyboardMarkup);
                adminSessionForProduct.setCurrentStep(AdminSessionForProduct.Step.PRODUCT_SUBCATEGORY);
            }
            case PRODUCT_SUBCATEGORY -> {
                Subcategory subcategory = Subcategory.fromValue(messageText);
                adminSessionForProduct.setSubcategory(subcategory);
                sendProducts(String.valueOf(chatId), adminSessionForProduct.getProductCategory(), adminSessionForProduct.getSubcategory());
                sendAdminMenus(chatId);
                adminSessionForProduct.reset();
            }
        }
    }


    private ReplyKeyboardMarkup createKeyboardMarkup(List<String> options) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Create a row for each option
        for (String option : options) {
            KeyboardRow row = new KeyboardRow();
            row.add(option);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createKeyboardMarkup1(List<String> options) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Create a row for each option
        for (String option : options) {
            KeyboardRow row = new KeyboardRow();
            row.add(option);
            keyboard.add(row);
        }

        // Add a row for "Другое" button
        KeyboardRow otherRow = new KeyboardRow();
        otherRow.add("Другое");
        keyboard.add(otherRow);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }


    private List<String> getCategoryValues() {
        List<String> categoryValues = new ArrayList<>();
        for (Category category : Category.values()) {
            categoryValues.add(category.getValue());
        }
        return categoryValues;
    }

    private List<String> getCountryValues() {
        List<String> countryValues = new ArrayList<>();
        for (Country country : Country.values()) {
            countryValues.add(country.getDisplayName());
        }
        return countryValues;
    }


    private List<String> getSubcategoryValues() {
        List<String> subcategoryValues = new ArrayList<>();
        for (Subcategory subcategory : Subcategory.values()) {
            subcategoryValues.add(subcategory.getValue());
        }
        return subcategoryValues;
    }


    private void sendStartMessage(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), WELCOME_MESSAGE);
        InlineKeyboardMarkup keyboardMarkup = createStartKeyboardMarkup();
        message.setReplyMarkup(keyboardMarkup);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending start message", e);
        }
    }

    private void sendMainMenuMessage(String chatId) {
        SendMessage message = new SendMessage(chatId, MAIN_MENU_MESSAGE);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add("Каталог");
        row2.add("Корзина");


        row3.add("Часто задаваемые вопросы");
        row4.add("Доставка");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);


        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }

    private void sendFAQ(String chatId) {
        SendMessage message = new SendMessage(chatId, "Выберите вопрос");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("В чем плюсы заказывать одежду/гаджет из Кореи?");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Как заказать?");


        keyboard.add(row1);
        keyboard.add(row2);


        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendProductsByCategoryAndSubcategory(String chatId, Category category, Subcategory subcategory) {
        List<Product> products = productRepository.getProductsByCategoryAndSubcategory(category, subcategory);
        if (products.isEmpty()) {
            sendMessage(chatId, "Нет доступных товаров в выбранной категории и подкатегории.");
        } else {
            for (Product product : products) {
                String message = "Название: " + product.getName() + "\nЦена: " + product.getPrice();
                sendMessage(chatId, message);


                InlineKeyboardMarkup keyboardMarkup = createAddToCartKeyboard(product.getId());
                sendProductImageWithKeyboard(chatId, product.getImageUrl(), keyboardMarkup);

            }
        }
    }

    private void sendProducts(String chatId, Category category, Subcategory subcategory) {
        List<Product> products = productRepository.getProductsByCategoryAndSubcategory(category, subcategory);
        if (products.isEmpty()) {
            sendMessage(chatId, "Нет доступных товаров в выбранной категории и подкатегории.");
        } else {
            for (Product product : products) {
                String message = "Название: " + product.getName() + "\nЦена: " + product.getPrice();
                String category1 = "Категория: " + product.getCategory().getValue() + "\nПодкатегория: " + product.getSubcategory().getValue();
                sendMessage(chatId, message);
                sendMessage(chatId, category1);
                // Отправить изображение товара
                InlineKeyboardMarkup keyboardMarkup = createDeleteKeyboard(product.getId());
                sendProductImageWithKeyboard(chatId, product.getImageUrl(), keyboardMarkup);

            }
        }

    }


    private void sendProductsInCart(String chatId) {
        User user = userRepository.findById(Long.valueOf(chatId)).orElse(null);
        if (user == null) {
            sendMessage(chatId, "Пользователь не найден. Пожалуйста, повторите попытку.");
            return;
        }
        List<Product_User> productUsers = userProductRepository.findByUser(user);
        if (productUsers.isEmpty()) {
            sendMessage(chatId, "Корзина пуста.");
        } else {
            for (Product_User productUser : productUsers) {
                Product product = productUser.getProduct();
                String message = "Название: " + product.getName() + "\nЦена: " + product.getPrice();
                sendMessage(chatId, message);
                InlineKeyboardMarkup keyboardMarkup = createDeleteKeyboardInCart(product.getId());
                sendProductImageWithKeyboard(chatId, product.getImageUrl(), keyboardMarkup);
            }
            SendMessage message = new SendMessage(chatId, "Нажмите для того чтобы оформить заказ!");
            ReplyKeyboardMarkup keyboardMarkup = createAddToOrderKeyboard();
            message.setReplyMarkup(keyboardMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendProductsInAdmin(String chatId) {
        User user = userRepository.findById(Long.valueOf(chatId)).orElse(null);
        if (user == null) {
            sendMessage(chatId, "Пользователь не найден. Пожалуйста, повторите попытку.");
            return;
        }
        List<Product_User> productUsers = userProductRepository.findByUser(user);
        if (productUsers.isEmpty()) {
            sendMessage(chatId, "Корзина пуста.");
        } else {
            for (Product_User productUser : productUsers) {
                Product product = productUser.getProduct();
                String message = "Название: " + product.getName() + "\nЦена: " + product.getPrice();
                sendMessage(chatId, message);

                sendProductImage(chatId, product.getImageUrl());
            }


        }
    }


    private InlineKeyboardMarkup createAddToCartKeyboard(Long productId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton addToCartButton = new InlineKeyboardButton("Добавить в корзину");
        addToCartButton.setCallbackData("addToCart_" + productId);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addToCartButton);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createAddToOrderKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true); // Optional: Set to 'false' if you want the keyboard to be persistent.

        // Create a list to hold keyboard rows.
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Create a new keyboard row.
        KeyboardRow row = new KeyboardRow();

        // Create the button text (you can customize this).
        String buttonText = "Оформить заказ";

        // Create the button and add it to the row.
        KeyboardButton button = new KeyboardButton(buttonText);
        row.add(button);

        // Add the row to the keyboard.
        keyboard.add(row);

        // Set the keyboard to the markup.
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }


    private InlineKeyboardMarkup createDeleteKeyboard(Long productId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton addToCartButton = new InlineKeyboardButton("Удалить");
        addToCartButton.setCallbackData("Delete" + productId);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addToCartButton);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup createDeleteKeyboardInCart(Long productId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton addToCartButton = new InlineKeyboardButton("Удалить из корзины");
        addToCartButton.setCallbackData("delete_cart" + productId);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addToCartButton);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }


    private void sendProductImageWithKeyboard(String chatId, String imageUrl, InlineKeyboardMarkup keyboardMarkup) {
        byte[] imageBytes = Base64.getDecoder().decode(imageUrl);
        InputFile inputFile = new InputFile(new ByteArrayInputStream(imageBytes), "product_image.jpg");
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при отправке изображения товара.");
        }
    }

    private void sendProductImage(String chatId, String imageUrl) {
        byte[] imageBytes = Base64.getDecoder().decode(imageUrl);
        InputFile inputFile = new InputFile(new ByteArrayInputStream(imageBytes), "product_image.jpg");
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(inputFile);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при отправке изображения товара.");
        }
    }


    private void sendMessage1(String chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage(chatId, text);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private String getMainMenu() {
        return MAIN_MENU_MESSAGE;
    }


    private void sendAdminStartMessage(String chatId) {
        SendMessage message = new SendMessage(chatId, "Добро пожаловать, администратор!");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить товар");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Все товары");


        keyboard.add(row1);
        keyboard.add(row2);


        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void sendAdminMenu(String chatId, String name) {
        SendMessage message = new SendMessage(chatId, "Товар " + name + " добавлен в базу данных.");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить товар");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Все товары");


        keyboard.add(row1);
        keyboard.add(row2);


        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void sendAdminMenus(String chatId) {
        SendMessage message = new SendMessage(chatId, "Товары");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить товар");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Все товары");


        keyboard.add(row1);
        keyboard.add(row2);


        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();


        }

    }


    private boolean isAdmin(String chatId) {
        return chatId.equals(ADMIN_CHAT_ID1) || chatId.equals(ADMIN_CHAT_ID2);
    }


    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}

