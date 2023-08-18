    package com.example.maxbuyer_shop_bot.entity;

    import lombok.Getter;
    import lombok.Setter;

    import javax.persistence.*;
    import java.util.List;

    @Getter
    @Setter
    @Entity(name = "product")
    public class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String price;
        private Category category;
        private Subcategory subcategory;
        private String imageUrl;


        @Override
        public String toString() {
            return "Название: " + name +
                    "\nЦена: " + price +
                    "\nКартинка: <img src=\"data:image/jpeg;base64," + imageUrl + "\">";
        }

    }
