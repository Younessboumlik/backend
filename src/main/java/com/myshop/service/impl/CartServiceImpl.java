package com.myshop.service.impl;

import com.myshop.domain.entity.CartItem;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.dto.request.CartItemRequest;
import com.myshop.dto.response.CartItemResponse;
import com.myshop.mapper.DtoMapper;
import com.myshop.repository.CartItemRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.UserRepository;
import com.myshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Long userId) {
        ensureUserExists(userId);
        return cartItemRepository.findByUserId(userId).stream()
                .map(DtoMapper::toCartItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public CartItemResponse addItem(Long userId, CartItemRequest request) {
        User user = ensureUserExists(userId);
        Product product = ensureProductExists(request.getProductId());
        validateStock(product, request.getQuantity());

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                .map(existing -> {
                    int newQuantity = existing.getQuantity() + request.getQuantity();
                    validateStock(product, newQuantity);
                    existing.setQuantity(newQuantity);
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(request.getQuantity())
                        .build());

        return DtoMapper.toCartItemResponse(cartItemRepository.save(cartItem));
    }

    @Override
    @Transactional
    public CartItemResponse updateItem(Long userId, Long cartItemId, CartItemRequest request) {
        ensureUserExists(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .filter(item -> item.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        Product product = ensureProductExists(request.getProductId());
        validateStock(product, request.getQuantity());

        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());
        return DtoMapper.toCartItemResponse(cartItemRepository.save(cartItem));
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        ensureUserExists(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .filter(item -> item.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        ensureUserExists(userId);
        cartItemRepository.findByUserId(userId)
                .forEach(cartItemRepository::delete);
    }

    private User ensureUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Product ensureProductExists(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (product.getStockQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is out of stock");
        }
        return product;
    }

    private void validateStock(Product product, Integer requestedQty) {
        if (requestedQty == null || requestedQty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
        if (requestedQty > product.getStockQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity exceeds stock");
        }
    }
}

