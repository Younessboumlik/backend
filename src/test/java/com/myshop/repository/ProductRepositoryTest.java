package com.myshop.repository;

import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Category testCategory;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();
        testCategory = entityManager.persistAndFlush(testCategory);

        testProduct1 = Product.builder()
                .category(testCategory)
                .name("Laptop")
                .description("Portable 14 pouces")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .createdAt(Instant.now())
                .build();
        testProduct1 = entityManager.persistAndFlush(testProduct1);

        testProduct2 = Product.builder()
                .category(testCategory)
                .name("Smartphone")
                .description("Téléphone intelligent")
                .price(new BigDecimal("599.99"))
                .stockQuantity(20)
                .createdAt(Instant.now())
                .build();
        testProduct2 = entityManager.persistAndFlush(testProduct2);
    }

    @Test
    void testSearchProducts_ByCategory() {
        List<Product> products = productRepository.searchProducts(
                testCategory.getId(), null, null, null);

        assertNotNull(products);
        assertEquals(2, products.size());
    }

    @Test
    void testSearchProducts_ByPriceRange() {
        List<Product> products = productRepository.searchProducts(
                null, new BigDecimal("500"), new BigDecimal("1000"), null);

        assertNotNull(products);
        assertEquals(2, products.size());
    }

    @Test
    void testSearchProducts_BySearchTerm() {
        List<Product> products = productRepository.searchProducts(
                null, null, null, "Laptop");

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    void testExistsByCategoryId() {
        assertTrue(productRepository.existsByCategoryId(testCategory.getId()));
        assertFalse(productRepository.existsByCategoryId(999L));
    }
}

