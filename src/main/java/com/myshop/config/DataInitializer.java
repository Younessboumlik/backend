package com.myshop.config;

import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.UserRole;
import com.myshop.repository.CategoryRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedData(UserRepository users, CategoryRepository categories, ProductRepository products, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmail("client@myshop.test")) {
                User user = User.builder()
                        .fullName("Client Test")
                        .email("client@myshop.test")
                        .passwordHash(encoder.encode("password"))
                        .phoneNumber("0600000000")
                        .address("Adresse de test")
                        .role(UserRole.CLIENT)
                        .build();
                users.save(user);
            }

            Category cat;
            if (!categories.existsByNameIgnoreCase("Electronique")) {
                cat = Category.builder()
                        .name("Electronique")
                        .description("Appareils et accessoires")
                        .build();
                cat = categories.save(cat);
            } else {
                cat = categories.findAll().stream().filter(c -> c.getName().equalsIgnoreCase("Electronique")).findFirst().orElseGet(() -> categories.findAll().stream().findFirst().orElse(null));
            }

            if (cat != null && !products.existsByCategoryId(cat.getId())) {
                products.save(Product.builder()
                        .category(cat)
                        .name("Laptop 14")
                        .description("Portable 14 pouces")
                        .price(new BigDecimal("8999.00"))
                        .stockQuantity(10)
                        .imageUrl("https://via.placeholder.com/300x200")
                        .build());

                products.save(Product.builder()
                        .category(cat)
                        .name("Casque Bluetooth")
                        .description("Casque sans fil")
                        .price(new BigDecimal("599.00"))
                        .stockQuantity(25)
                        .imageUrl("https://via.placeholder.com/300x200")
                        .build());
            }
        };
    }
}
