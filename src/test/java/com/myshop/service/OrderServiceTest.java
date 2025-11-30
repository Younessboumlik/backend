package com.myshop.service;

import com.myshop.domain.entity.CartItem;
import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Order;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.OrderPaymentMethod;
import com.myshop.domain.enums.OrderStatus;
import com.myshop.domain.enums.PaymentStatus;
import com.myshop.domain.enums.UserRole;
import com.myshop.dto.request.CheckoutRequest;
import com.myshop.dto.request.OrderStatusUpdateRequest;
import com.myshop.dto.response.OrderResponse;
import com.myshop.repository.CartItemRepository;
import com.myshop.repository.OrderRepository;
import com.myshop.repository.PaymentRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.UserRepository;
import com.myshop.service.impl.OrderServiceImpl;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Category testCategory;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
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

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .totalAmount(new BigDecimal("1999.98"))
                .orderStatus(OrderStatus.PROCESSING)
                .paymentMethod(OrderPaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName("John Doe")
                .shippingAddress("123 Main St")
                .shippingPhone("0600000000")
                .shippingEmail("john@example.com")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCheckout_Success() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(1L);
        request.setShippingName("John Doe");
        request.setShippingAddress("123 Main St");
        request.setShippingPhone("0600000000");
        request.setShippingEmail("john@example.com");
        request.setPaymentMethod(OrderPaymentMethod.CASH_ON_DELIVERY);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.checkout(request);

        assertNotNull(response);
        verify(userRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).findByUserId(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCheckout_EmptyCart() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () -> orderService.checkout(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testGetOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrder(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateOrderStatus_Success() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setOrderStatus(OrderStatus.PAID);
        request.setPaymentStatus(PaymentStatus.SUCCESS);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.updateOrderStatus(1L, request);

        assertNotNull(response);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testGetOrdersForUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));

        List<OrderResponse> responses = orderService.getOrdersForUser(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(orderRepository, times(1)).findByUserId(1L);
    }
}

