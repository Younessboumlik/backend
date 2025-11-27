package com.myshop.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OrderItemResponse {
    Long id;
    Long productId;
    String productName;
    Integer quantity;
    BigDecimal unitPrice;
}

