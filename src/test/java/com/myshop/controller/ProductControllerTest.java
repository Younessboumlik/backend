package com.myshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshop.dto.request.CreateProductRequest;
import com.myshop.dto.request.UpdateProductRequest;
import com.myshop.dto.response.CategoryResponse;
import com.myshop.dto.response.ProductResponse;
import com.myshop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetProduct_Success() throws Exception {
        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .category(category)
                .name("Laptop")
                .description("Portable 14 pouces")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .imageUrl("https://example.com/laptop.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(productService.getProduct(1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(1L);
        request.setName("Smartphone");
        request.setDescription("Téléphone intelligent");
        request.setPrice(new BigDecimal("599.99"));
        request.setStockQuantity(20);

        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .category(category)
                .name("Smartphone")
                .description("Téléphone intelligent")
                .price(new BigDecimal("599.99"))
                .stockQuantity(20)
                .imageUrl("https://example.com/phone.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Smartphone"));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setCategoryId(1L);
        request.setName("Updated Laptop");
        request.setPrice(new BigDecimal("1099.99"));
        request.setStockQuantity(15);

        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .category(category)
                .name("Updated Laptop")
                .description("Portable 14 pouces mis à jour")
                .price(new BigDecimal("1099.99"))
                .stockQuantity(15)
                .imageUrl("https://example.com/updated-laptop.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSearchProducts_Success() throws Exception {
        CategoryResponse category = CategoryResponse.builder()
                .id(1L)
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();

        ProductResponse product1 = ProductResponse.builder()
                .id(1L)
                .category(category)
                .name("Laptop")
                .description("Portable 14 pouces")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .imageUrl("https://example.com/laptop.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProductResponse product2 = ProductResponse.builder()
                .id(2L)
                .category(category)
                .name("Smartphone")
                .description("Téléphone intelligent")
                .price(new BigDecimal("599.99"))
                .stockQuantity(20)
                .imageUrl("https://example.com/phone.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<ProductResponse> products = Arrays.asList(product1, product2);

        when(productService.searchProducts(any(), any(), any(), any(), any())).thenReturn(products);

        mockMvc.perform(get("/api/products")
                        .param("categoryId", "1")
                        .param("minPrice", "500")
                        .param("maxPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

