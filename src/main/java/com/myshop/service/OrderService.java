package com.myshop.service;

import com.myshop.dto.request.CheckoutRequest;
import com.myshop.dto.request.OrderStatusUpdateRequest;
import com.myshop.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse checkout(CheckoutRequest request);
    OrderResponse getOrder(Long id);
    List<OrderResponse> getOrdersForUser(Long userId);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}

