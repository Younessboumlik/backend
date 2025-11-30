package com.myshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myshop.dto.request.CreateUserRequest;
import com.myshop.dto.response.UserResponse;
import com.myshop.domain.enums.UserRole;
import com.myshop.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRole(UserRole.CLIENT);

        UserResponse response = UserResponse.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetUser_Success() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();

        when(userService.getUser(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        UserResponse user1 = UserResponse.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();

        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .role(UserRole.ADMIN)
                .createdAt(Instant.now())
                .build();

        List<UserResponse> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

