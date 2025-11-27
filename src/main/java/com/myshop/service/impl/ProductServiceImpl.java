package com.myshop.service.impl;

import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Product;
import com.myshop.dto.request.CreateProductRequest;
import com.myshop.dto.request.UpdateProductRequest;
import com.myshop.dto.response.ProductResponse;
import com.myshop.mapper.DtoMapper;
import com.myshop.repository.CategoryRepository;
import com.myshop.repository.OrderItemRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(Long categoryId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                String search,
                                                Sort sort) {
        List<Product> products = productRepository.searchProducts(categoryId, minPrice, maxPrice,
                search != null ? search.trim() : null);

        if (sort != null && sort.isSorted()) {
            Comparator<Product> comparator = null;
            for (Sort.Order order : sort) {
                Comparator<Product> propertyComparator = comparatorFor(order.getProperty());
                if (propertyComparator == null) {
                    continue;
                }
                if (order.isDescending()) {
                    propertyComparator = propertyComparator.reversed();
                }
                comparator = comparator == null
                        ? propertyComparator
                        : comparator.thenComparing(propertyComparator);
            }
            if (comparator != null) {
                products = products.stream().sorted(comparator).toList();
            }
        }

        return products.stream()
                .map(DtoMapper::toProductResponse)
                .toList();
    }

    private Comparator<Product> comparatorFor(String property) {
        if (property == null) {
            return null;
        }
        return switch (property) {
            case "price" -> Comparator.comparing(Product::getPrice);
            case "createdAt" -> Comparator.comparing(Product::getCreatedAt);
            case "name" -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            default -> null;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return DtoMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        validatePriceAndStock(request.getPrice(), request.getStockQuantity());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        return DtoMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        validatePriceAndStock(request.getPrice(), request.getStockQuantity());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        return DtoMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (orderItemRepository.existsByProductId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete product linked to an order");
        }
        productRepository.delete(product);
    }

    private void validatePriceAndStock(BigDecimal price, Integer stockQuantity) {
        if (price == null || price.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be greater than zero");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity must be positive");
        }
    }
}

