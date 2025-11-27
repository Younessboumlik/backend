package com.myshop.mapper;

import com.myshop.domain.entity.CartItem;
import com.myshop.domain.entity.Category;
import com.myshop.domain.entity.Order;
import com.myshop.domain.entity.OrderItem;
import com.myshop.domain.entity.Payment;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.dto.response.CartItemResponse;
import com.myshop.dto.response.CategoryResponse;
import com.myshop.dto.response.OrderItemResponse;
import com.myshop.dto.response.OrderResponse;
import com.myshop.dto.response.PaymentResponse;
import com.myshop.dto.response.ProductResponse;
import com.myshop.dto.response.UserResponse;

import java.math.BigDecimal;
import java.util.List;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .category(toCategoryResponse(product.getCategory()))
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static CartItemResponse toCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(product.getImageUrl())
                .unitPrice(product.getPrice())
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .build();
    }

    public static PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .transactionReference(payment.getTransactionReference())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    public static OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(DtoMapper::toOrderItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingName(order.getShippingName())
                .shippingAddress(order.getShippingAddress())
                .shippingPhone(order.getShippingPhone())
                .shippingEmail(order.getShippingEmail())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .payment(toPaymentResponse(order.getPayment()))
                .build();
    }
}

