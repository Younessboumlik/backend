package com.myshop.service;

import com.myshop.domain.entity.User;
import com.myshop.domain.enums.UserRole;
import com.myshop.dto.request.CreateUserRequest;
import com.myshop.dto.response.UserResponse;
import com.myshop.repository.UserRepository;
import com.myshop.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("encodedPassword")
                .phoneNumber("0600000000")
                .address("123 Main St")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCreateUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setPhoneNumber("0611111111");
        request.setAddress("456 Oak Ave");
        request.setRole(UserRole.CLIENT);

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        verify(userRepository, times(1)).existsByEmail("jane@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRole(UserRole.CLIENT);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUser(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john@example.com", response.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getUser(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testGetAllUsers_Success() {
        User user2 = User.builder()
                .id(2L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .passwordHash("encodedPassword2")
                .role(UserRole.ADMIN)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(userRepository, times(1)).findAll();
    }
}

