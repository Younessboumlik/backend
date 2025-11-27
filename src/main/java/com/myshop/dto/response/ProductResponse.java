package com.myshop.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class ProductResponse {
    Long id;
    CategoryResponse category;
    String name;
    String description;
    BigDecimal price;
    Integer stockQuantity;
    String imageUrl;
    Instant createdAt;
    Instant updatedAt;
}

