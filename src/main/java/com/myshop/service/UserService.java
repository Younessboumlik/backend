package com.myshop.service;

import com.myshop.dto.request.CreateUserRequest;
import com.myshop.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUser(Long id);
    List<UserResponse> getAllUsers();
}

