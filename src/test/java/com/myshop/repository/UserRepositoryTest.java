package com.myshop.repository;

import com.myshop.domain.entity.User;
import com.myshop.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("encodedPassword")
                .phoneNumber("0600000000")
                .address("123 Main St")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void testFindById_Success() {
        Optional<User> found = userRepository.findById(testUser.getId());

        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void testExistsByEmail_Success() {
        assertTrue(userRepository.existsByEmail("john@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testSaveUser_Success() {
        User newUser = User.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .passwordHash("encodedPassword2")
                .role(UserRole.ADMIN)
                .createdAt(Instant.now())
                .build();

        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("jane@example.com", saved.getEmail());
    }
}

