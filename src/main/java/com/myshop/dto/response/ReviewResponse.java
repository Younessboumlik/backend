package com.myshop.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ReviewResponse {
    Long id;
    Long productId;
    Long userId;
    Integer rating;
    String comment;
    Instant createdAt;
}

