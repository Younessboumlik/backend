package com.myshop.service;

import com.myshop.dto.request.CreateProductRequest;
import com.myshop.dto.request.UpdateProductRequest;
import com.myshop.dto.response.ProductResponse;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<ProductResponse> searchProducts(Long categoryId,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         String search,
                                         Sort sort);

    ProductResponse getProduct(Long id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);
}

