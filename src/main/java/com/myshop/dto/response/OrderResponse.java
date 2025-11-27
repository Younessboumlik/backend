package com.myshop.dto.response;

import com.myshop.domain.enums.OrderPaymentMethod;
import com.myshop.domain.enums.OrderStatus;
import com.myshop.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class OrderResponse {
    Long id;
    Long userId;
    BigDecimal totalAmount;
    OrderStatus orderStatus;
    OrderPaymentMethod paymentMethod;
    PaymentStatus paymentStatus;
    String shippingName;
    String shippingAddress;
    String shippingPhone;
    String shippingEmail;
    Instant createdAt;
    Instant updatedAt;
    List<OrderItemResponse> items;
    PaymentResponse payment;
}

