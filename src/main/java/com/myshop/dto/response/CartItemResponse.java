package com.myshop.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class CartItemResponse {
    Long id;
    Long productId;
    String productName;
    String imageUrl;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal lineTotal;
}

