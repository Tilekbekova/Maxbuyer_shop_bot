package com.example.maxbuyer_shop_bot.config;

import com.example.maxbuyer_shop_bot.admin.AdminProductService;
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
    private UserSessionManager userSessionManager;
    private static final String BOT_TOKEN = "6339506277:AAEHCDapOS_gOLsHbbW054bDSxLhxjFLUJE";

    private static final String WELCOME_MESSAGE = "Доброе пожаловать в Maxbuyer!!! Давайте знакомиться – меня зовут Максим, я являюсь байером в Корее. Занимаюсь выкупом оригинальных товаров и осуществляю доставку по всему миру!\nДля того, чтобы смотреть весь ассортимент, нажмите кнопку «START»!";
    private static final String MAIN_MENU_MESSAGE = "Выберите один из пунктов главного меню:";
    private static final String ADMIN_CHAT_ID = "1068426745";
    private static final String FIRST_QUESTION = "1) ВЫГОДА - чаще всего заплатив мне за мои услуги байера и доставку , Вы все равно сэкономите свои деньги , так как многие товары в Корее дешевле чем в странах СНГ ✅\n" +
            "2) БРАК - Я лично нахожусь в Корее и любой отправляемый товар проходит проверку на качество , после чего отправляется ✅\n" +
            "3) ЭКСКЛЮЗИВ - по мимо того что в наших странах завышенные цены , так еще и очень часто покупатель сталкивается с проблемой выбора , его банально нет. В Корее Вы можете найти одежду и многое другое , чего точно нет ни у кого в вашем городе✅\n" +
            "4) БАЙЕР - от байера зависит очень многое , Я полностью проведу Вас онлайн  -  от выбора товара —> до отправки в ваш город✅";
    private static final String SECOND_QUESTION = "1. Если вас интересует что-то конкретное: 1) отправляете мне фото или название того что вас интересует 2) далее, я нахожу то что вам нужно (отправляю вам Фото или видео с оф магазина или отправляю вам ссылку на оф сайт), если вас устраивает цена в корее, 3) вы отправляете мне деньги 100% от стоимости, Я выкупаю ваш товар и отправляю в ваш город. \n" +
            " Удобно выгодно прозрачно√ 2. Если вы не отпределились с выбором: \n" +
            "1) вы так же можете обратиться ко мне, если не знаете наверняка чего хотите. я полностью вас проконсультирую и помогу определиться с выбором 2) подберу ваши размеры одежда/обувь, найду для вас любую технику/гаджет \n" +
            "3) проведу вас онлайн -> от выбора, до покупки и отправки в ваш город. На связи 24/7√";

    private static final String DELIVERY = "Я использую ЕМС доставку - это один из самых надежных и быстрых способов отправить посылку в страны СНГ. \n" +
            "\n" +
            "Сроки доставки от 4-14 дней (зависимости от страны)";
    private final AdminProductService adminProductService;


    private static final String[] CATEGORIES = {"Брюки/шорты/юбки", "Платья", "Рубашки/топы", "Верхняя одежда", "Избранное"};
    private static final String[] SUBCATEGORIES = {"Мужские", "Женские"};

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
            long chatId1 = message.getChatId();
            if (message.hasText()) {


                String messageText = message.getText();
                long chatId = message.getChatId();

                if (messageText.equalsIgnoreCase("/start")) {
                    if (isAdmin(String.valueOf(chatId))) {
                        sendAdminStartMessage(String.valueOf(chatId));
                    } else {
                        sendStartMessage(chatId);
                        updateDB(chatId, message.getFrom().getUserName());

                    }
                } else if (messageText.equalsIgnoreCase("/menu")) {
                    sendMessage(String.valueOf(chatId), getMainMenu());
                } else if (messageText.equalsIgnoreCase("Часто задаваемые вопросы")) {
                    sendFAQ(String.valueOf(chatId));
                } else if (messageText.equalsIgnoreCase("Доставка")) {
                    sendMessage(String.valueOf(chatId), DELIVERY);
                } else if (messageText.equalsIgnoreCase("в чем плюсы заказывать одежду/гаджет из Кореи?")) {
                    sendMessage(String.valueOf(chatId), FIRST_QUESTION);
                    createBack(String.valueOf(chatId));
                } else if (messageText.equalsIgnoreCase("как заказать?")) {
                    sendMessage(String.valueOf(chatId), SECOND_QUESTION);
                    createBack(String.valueOf(chatId));
                } else if (messageText.equalsIgnoreCase("Корзина")) {
                    sendProductsInCart(String.valueOf(chatId));
                    createBack(String.valueOf(chatId));
                } else if (isAdmin(String.valueOf(chatId)) && messageText.equalsIgnoreCase("Все продукты")) {
                    sendProducts(String.valueOf(chatId));


                } else if (messageText.equalsIgnoreCase("Каталог")) {
                    List<String> categoryValues = getCategoryValues();
                    ReplyKeyboardMarkup categoryKeyboardMarkup = createKeyboardMarkup(categoryValues);
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
                } else if (isAdmin(String.valueOf(chatId)) && messageText.equalsIgnoreCase("Добавить товар")) {
                    sendMessage(String.valueOf(chatId), "Введите имя товара:");
                    adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_NAME);
                } else {
                    handleAdminConversation(String.valueOf(chatId), update);
                }
            } else if (message.hasPhoto()) {
                handleProductImage(String.valueOf(chatId1), message);
            } else {
                sendMessage(String.valueOf(chatId1), "Извините, не могу распознать ваш запрос. Пожалуйста, повторите.");
            }
        } else if (update.hasCallbackQuery()) {

            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();

            if (callbackData.equalsIgnoreCase("Start")) {
                sendMainMenuMessage(String.valueOf(chatId));
            } else if (callbackData.equalsIgnoreCase("Back")) {
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

            } else if (callbackData.startsWith("add_to_order")) {
                User user = userRepository.findById(chatId).orElseGet(User::new);
                userSessionManager.setCurrentStep(UserSessionManager.Step.ENTER_USER_COUNTRY);
                List<String> countryValues = getCountryValues();
                ReplyKeyboardMarkup countryKeyboardMarkup = createKeyboardMarkup(countryValues);
                sendMessage1(String.valueOf(chatId), "Из какой вы страны?", countryKeyboardMarkup);
            } else if (userSessionManager.getCurrentStep() == UserSessionManager.Step.ENTER_USER_COUNTRY && update.hasMessage() && update.getMessage().hasText()) {
                User user = userRepository.findById(chatId).orElseGet(User::new);
                String userCountry = update.getMessage().getText();
                user.setCountry(Country.valueOf(userCountry));
                userRepository.save(user);
                // Perform any necessary actions with the user's country selection
                // You can also update the user's session step if needed
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


            }
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

    private void sendSelectedProductsToAdmin(String chatId, List<Product_User> productUsers, String userName, Country selectedCountry) {
        // Отправка выбранных продуктов администратору
        StringBuilder message = new StringBuilder("Заказ пользователя " + chatId + ":\n");
        message.append("Страна: ").append(selectedCountry.getDisplayName()).append("\n\n");


        for (Product_User productUser : productUsers) {
            Product product = productUser.getProduct();
            message.append("Название: ").append(product.getName()).append("\n");
            message.append("Цена: ").append(product.getPrice()).append("\n\n");
        }

        SendMessage adminMessage = new SendMessage(ADMIN_CHAT_ID, message.toString());

        // Создайте InlineKeyboardMarkup с кнопкой "Перейти в ЛС"
        InlineKeyboardMarkup keyboardMarkup = createGoToPrivateChatKeyboard(chatId, userName);
        adminMessage.setReplyMarkup(keyboardMarkup);
        sendMessage(chatId, "Заказ отправлен с вами свяжутся");

        try {
            execute(adminMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup createGoToPrivateChatKeyboard(String chatId, String userName) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton goToPrivateChatButton = new InlineKeyboardButton("Перейти в ЛС");

        String privateChatUrl;
        if (userName != null && !userName.isEmpty()) {
            privateChatUrl = "https://t.me/" + userName;
        } else {
            privateChatUrl = "https://t.me/user?id=" + chatId;
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
                double price;
                try {
                    price = Double.parseDouble(messageText);
                    adminSessionManager.setProductPrice(price);


                    List<String> categoryValues = getCategoryValues();


                    ReplyKeyboardMarkup categoryKeyboardMarkup = createKeyboardMarkup(categoryValues);


                    sendMessage1(chatId, "Выберите категорию товара:", categoryKeyboardMarkup);
                    adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_CATEGORY);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Некорректная цена. Пожалуйста, введите числовое значение.");
                }
                break;
            case ENTER_PRODUCT_CATEGORY:

                String categoryValue = messageText;
                try {
                    Category category = Category.fromValue(categoryValue);
                    adminSessionManager.setProductCategory(category);


                    List<String> subcategoryValues = getSubcategoryValues();


                    ReplyKeyboardMarkup subcategoryKeyboardMarkup = createKeyboardMarkup(subcategoryValues);

                    sendMessage1(chatId, "Выберите подкатегорию товара:", subcategoryKeyboardMarkup);
                    adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_SUBCATEGORY);
                } catch (IllegalArgumentException e) {
                    sendMessage(chatId, "Некорректная категория. Пожалуйста, выберите категорию из предложенных вариантов.");
                }
                break;
            case ENTER_PRODUCT_SUBCATEGORY:

                String subcategoryValue = messageText;
                try {
                    Subcategory subcategory = Subcategory.fromValue(subcategoryValue);
                    adminSessionManager.setSubcategory(subcategory);

                    sendMessage(chatId, "Отправьте картинку товара:");
                    adminSessionManager.setCurrentStep(AdminSessionManager.Step.ENTER_PRODUCT_IMAGE);
                } catch (IllegalArgumentException e) {
                    sendMessage(chatId, "Некорректная подкатегория. Пожалуйста, выберите подкатегорию из предложенных вариантов.");
                }
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
        row1.add("в чем плюсы заказывать одежду/гаджет из Кореи?");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("как заказать?");


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

    private void sendProducts(String chatId) {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            sendMessage(chatId, "Нет доступных товаров.");
        } else {
            for (Product product : products) {
                // Отправить сообщение с текстовыми данными о товаре
                String message = "Название: " + product.getName() + "\nЦена: " + product.getPrice();
                String category = "Категория: " + product.getCategory().getValue() + "\nПодкатегория: " + product.getSubcategory().getValue();
                sendMessage(chatId, message);
                sendMessage(chatId, category);
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
            InlineKeyboardMarkup keyboardMarkup = createAddToOrderKeyboard();
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

    private InlineKeyboardMarkup createAddToOrderKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton addToOrderButton = new InlineKeyboardButton("Оформить заказ");
        addToOrderButton.setCallbackData("add_to_order");
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addToOrderButton);
        keyboard.add(row);
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
        row2.add("Все продукты");


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
        return chatId.equals("1068426745");
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

