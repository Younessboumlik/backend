package com.myshop.service;

import com.myshop.dto.request.CartItemRequest;
import com.myshop.dto.response.CartItemResponse;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getCartItems(Long userId);
    CartItemResponse addItem(Long userId, CartItemRequest request);
    CartItemResponse updateItem(Long userId, Long cartItemId, CartItemRequest request);
    void removeItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}

