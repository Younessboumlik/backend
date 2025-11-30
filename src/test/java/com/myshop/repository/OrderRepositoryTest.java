package com.myshop.repository;

import com.myshop.domain.entity.Order;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.OrderPaymentMethod;
import com.myshop.domain.enums.OrderStatus;
import com.myshop.domain.enums.PaymentStatus;
import com.myshop.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("encodedPassword")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        testOrder = Order.builder()
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
        testOrder = entityManager.persistAndFlush(testOrder);
    }

    @Test
    void testFindByUserId_Success() {
        List<Order> orders = orderRepository.findByUserId(testUser.getId());

        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(testOrder.getId(), orders.get(0).getId());
    }

    @Test
    void testFindById_Success() {
        Order found = orderRepository.findById(testOrder.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(testOrder.getTotalAmount(), found.getTotalAmount());
        assertEquals(OrderStatus.PROCESSING, found.getOrderStatus());
    }

    @Test
    void testSaveOrder_Success() {
        Order newOrder = Order.builder()
                .user(testUser)
                .totalAmount(new BigDecimal("599.99"))
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(OrderPaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName("Jane Doe")
                .shippingAddress("456 Oak Ave")
                .shippingPhone("0611111111")
                .shippingEmail("jane@example.com")
                .createdAt(Instant.now())
                .build();

        Order saved = orderRepository.save(newOrder);

        assertNotNull(saved.getId());
        assertEquals(new BigDecimal("599.99"), saved.getTotalAmount());
    }
}

