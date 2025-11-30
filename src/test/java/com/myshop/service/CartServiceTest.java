package com.myshop.service;

import com.myshop.domain.entity.CartItem;
import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.UserRole;
import com.myshop.dto.request.CartItemRequest;
import com.myshop.dto.response.CartItemResponse;
import com.myshop.repository.CartItemRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.UserRepository;
import com.myshop.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Category testCategory;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("encoded")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Electronique")
                .createdAt(Instant.now())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .category(testCategory)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .createdAt(Instant.now())
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(2)
                .build();
    }

    @Test
    void testGetCartItems_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem));

        List<CartItemResponse> responses = cartService.getCartItems(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(userRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetCartItems_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cartService.getCartItems(999L));
    }

    @Test
    void testAddItem_Success_NewItem() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItemResponse response = cartService.addItem(1L, request);

        assertNotNull(response);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddItem_Success_UpdateExisting() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItemResponse response = cartService.addItem(1L, request);

        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    @Test
    void testAddItem_ProductOutOfStock() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        Product outOfStockProduct = Product.builder()
                .id(1L)
                .stockQuantity(0)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(outOfStockProduct));

        assertThrows(ResponseStatusException.class, () -> cartService.addItem(1L, request));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void testRemoveItem_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        cartService.removeItem(1L, 1L);

        verify(cartItemRepository, times(1)).delete(testCartItem);
    }

    @Test
    void testClearCart_Success() {
        CartItem cartItem2 = CartItem.builder()
                .id(2L)
                .user(testUser)
                .product(testProduct)
                .quantity(1)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem, cartItem2));

        cartService.clearCart(1L);

        verify(cartItemRepository, times(2)).delete(any(CartItem.class));
    }
}

