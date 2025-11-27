package com.myshop.dto.response;

import com.myshop.domain.enums.UserRole;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserResponse {
    Long id;
    String fullName;
    String email;
    String phoneNumber;
    String address;
    UserRole role;
    Instant createdAt;
}

