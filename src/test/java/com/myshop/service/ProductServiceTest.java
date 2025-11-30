package com.myshop.service;

import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import com.myshop.dto.request.CreateProductRequest;
import com.myshop.dto.request.UpdateProductRequest;
import com.myshop.dto.response.ProductResponse;
import com.myshop.repository.CategoryRepository;
import com.myshop.repository.OrderItemRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .category(testCategory)
                .name("Laptop")
                .description("Portable 14 pouces")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .imageUrl("https://example.com/laptop.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testGetProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProduct(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProduct_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> productService.getProduct(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateProduct_Success() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(1L);
        request.setName("Smartphone");
        request.setDescription("Téléphone intelligent");
        request.setPrice(new BigDecimal("599.99"));
        request.setStockQuantity(20);
        request.setImageUrl("https://example.com/phone.jpg");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_CategoryNotFound() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(999L);
        request.setName("Product");
        request.setPrice(new BigDecimal("100.00"));
        request.setStockQuantity(10);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProduct_InvalidPrice() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(1L);
        request.setName("Product");
        request.setPrice(new BigDecimal("-10.00"));
        request.setStockQuantity(10);

        assertThrows(ResponseStatusException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateProduct_Success() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setCategoryId(1L);
        request.setName("Updated Laptop");
        request.setDescription("Updated description");
        request.setPrice(new BigDecimal("1099.99"));
        request.setStockQuantity(15);
        request.setImageUrl("https://example.com/updated.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, request);

        assertNotNull(response);
        verify(productRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(false);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    void testDeleteProduct_LinkedToOrder() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> productService.deleteProduct(1L));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void testSearchProducts_WithFilters() {
        Product product2 = Product.builder()
                .id(2L)
                .category(testCategory)
                .name("Smartphone")
                .price(new BigDecimal("599.99"))
                .stockQuantity(5)
                .createdAt(Instant.now())
                .build();

        when(productRepository.searchProducts(1L, new BigDecimal("500"), new BigDecimal("1000"), "Laptop"))
                .thenReturn(Arrays.asList(testProduct, product2));

        List<ProductResponse> results = productService.searchProducts(
                1L, new BigDecimal("500"), new BigDecimal("1000"), "Laptop", Sort.unsorted());

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepository, times(1)).searchProducts(1L, new BigDecimal("500"), new BigDecimal("1000"), "Laptop");
    }
}

