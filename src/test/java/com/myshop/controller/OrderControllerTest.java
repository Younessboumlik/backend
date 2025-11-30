package com.myshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshop.dto.request.CheckoutRequest;
import com.myshop.dto.request.OrderStatusUpdateRequest;
import com.myshop.dto.response.OrderResponse;
import com.myshop.domain.enums.OrderPaymentMethod;
import com.myshop.domain.enums.OrderStatus;
import com.myshop.domain.enums.PaymentStatus;
import com.myshop.service.OrderService;
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

@WebMvcTest(controllers = OrderController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCheckout_Success() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(1L);
        request.setShippingName("John Doe");
        request.setShippingAddress("123 Main St");
        request.setShippingPhone("0600000000");
        request.setShippingEmail("john@example.com");
        request.setPaymentMethod(OrderPaymentMethod.CASH_ON_DELIVERY);

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(new BigDecimal("1999.98"))
                .orderStatus(OrderStatus.PROCESSING)
                .paymentMethod(OrderPaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName("John Doe")
                .shippingAddress("123 Main St")
                .shippingPhone("0600000000")
                .shippingEmail("john@example.com")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .items(List.of())
                .payment(null)
                .build();

        when(orderService.checkout(any(CheckoutRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderStatus").value("PROCESSING"));
    }

    @Test
    void testGetOrder_Success() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(new BigDecimal("1999.98"))
                .orderStatus(OrderStatus.PROCESSING)
                .createdAt(Instant.now())
                .build();

        when(orderService.getOrder(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testListOrders_Success() throws Exception {
        OrderResponse order1 = OrderResponse.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(new BigDecimal("1999.98"))
                .orderStatus(OrderStatus.PROCESSING)
                .paymentMethod(OrderPaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName("John Doe")
                .shippingAddress("123 Main St")
                .shippingPhone("0600000000")
                .shippingEmail("john@example.com")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .items(List.of())
                .payment(null)
                .build();

        List<OrderResponse> orders = Arrays.asList(order1);

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setOrderStatus(OrderStatus.PAID);
        request.setPaymentStatus(PaymentStatus.SUCCESS);

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderStatus(OrderStatus.PAID)
                .paymentStatus(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();

        when(orderService.updateOrderStatus(eq(1L), any(OrderStatusUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("PAID"));
    }
}

