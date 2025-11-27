package com.myshop.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CategoryResponse {
    Long id;
    String name;
    String description;
    Instant createdAt;
}

