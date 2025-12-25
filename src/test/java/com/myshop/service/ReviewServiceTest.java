package com.myshop.service;

import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.Review;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.UserRole;
import com.myshop.dto.request.ReviewRequest;
import com.myshop.dto.response.ReviewResponse;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.ReviewRepository;
import com.myshop.repository.UserRepository;
import com.myshop.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Category testCategory;
    private Product testProduct;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.CLIENT)
                .createdAt(Instant.now())
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Electronique")
                .createdAt(Instant.now())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .category(testCategory)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .createdAt(Instant.now())
                .build();

        testReview = Review.builder()
                .id(1L)
                .product(testProduct)
                .user(testUser)
                .rating(5)
                .comment("Excellent produit!")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCreateReview_Success() {
        ReviewRequest request = new ReviewRequest();
        request.setProductId(1L);
        request.setUserId(1L);
        request.setRating(5);
        request.setComment("Great product!");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReview_ProductNotFound() {
        ReviewRequest request = new ReviewRequest();
        request.setProductId(999L);
        request.setUserId(1L);
        request.setRating(5);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> reviewService.createReview(request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetReviewsForProduct_Success() {
        Review review2 = Review.builder()
                .id(2L)
                .product(testProduct)
                .user(testUser)
                .rating(4)
                .comment("Good")
                .createdAt(Instant.now())
                .build();

        when(reviewRepository.findByProductId(1L)).thenReturn(Arrays.asList(testReview, review2));

        List<ReviewResponse> responses = reviewService.getReviewsForProduct(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(reviewRepository, times(1)).findByProductId(1L);
    }

    @Test
    void testDeleteReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).delete(testReview);
    }

    @Test
    void testDeleteReview_NotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> reviewService.deleteReview(999L));
        verify(reviewRepository, never()).delete(any());
    }
}


