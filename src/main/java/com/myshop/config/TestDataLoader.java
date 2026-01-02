package com.myshop.config;

import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.UserRole;
import com.myshop.repository.CategoryRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TestDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataLoader(UserRepository userRepository,
                          ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("✅ Seeding test data...");

        // ----------- USER TEST -----------
        if (userRepository.findByEmail("abdellah@gmail.com").isEmpty()) {
            User user = new User();
            user.setFullName("Test User");
            user.setEmail("abdellah@gmail.com");
            user.setPasswordHash(passwordEncoder.encode("testtest"));
            user.setRole(UserRole.CLIENT);
            userRepository.save(user);
            System.out.println("✅ Test user created");
        }

        // ----------- CATEGORY TEST -----------
        Category category;
        if (categoryRepository.count() == 0) {
            category = new Category();
            category.setName("Catégorie Test");
            categoryRepository.save(category);
            System.out.println("✅ Test category created");
        }
        category = categoryRepository.findAll().get(0);


        // ----------- PRODUITS TEST -----------
        if (productRepository.count() == 0) {

            Product product1 = Product.builder()
                    .name("Produit Test 1")
                    .description("Description du produit test 1")
                    .price(new BigDecimal("100.00"))
                    .stockQuantity(10)
                    .category(category) // ⚠️ Obligatoire
                    .build();

            Product product2 = Product.builder()
                    .name("Produit Test 2")
                    .description("Description du produit test 2")
                    .price(new BigDecimal("50.00"))
                    .stockQuantity(20)
                    .category(category) // ⚠️ Obligatoire
                    .build();

            productRepository.save(product1);
            productRepository.save(product2);

            System.out.println("✅ Test products created");
        }
    }
}
