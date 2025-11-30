package com.myshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshop.dto.request.CartItemRequest;
import com.myshop.dto.response.CartItemResponse;
import com.myshop.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetCart_Success() throws Exception {
        CartItemResponse item1 = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        List<CartItemResponse> items = Arrays.asList(item1);

        when(cartService.getCartItems(1L)).thenReturn(items);

        mockMvc.perform(get("/api/users/1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testAddItem_Success() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        CartItemResponse response = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        when(cartService.addItem(eq(1L), any(CartItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void testUpdateItem_Success() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        CartItemResponse response = CartItemResponse.builder()
                .id(1L)
                .productId(1L)
                .quantity(3)
                .build();

        when(cartService.updateItem(eq(1L), eq(1L), any(CartItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1/cart/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void testRemoveItem_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1/cart/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testClearCart_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1/cart"))
                .andExpect(status().isNoContent());
    }
}

