package com.myshop.service;

import com.myshop.dto.request.ReviewRequest;
import com.myshop.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    List<ReviewResponse> getReviewsForProduct(Long productId);
    void deleteReview(Long reviewId);
}

