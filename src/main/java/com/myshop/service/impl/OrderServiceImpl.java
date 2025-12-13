package com.myshop.service.impl;

import com.myshop.domain.entity.CartItem;
import com.myshop.domain.entity.Order;
import com.myshop.domain.entity.OrderItem;
import com.myshop.domain.entity.Payment;
import com.myshop.domain.entity.Product;
import com.myshop.domain.entity.User;
import com.myshop.domain.enums.OrderPaymentMethod;
import com.myshop.domain.enums.OrderStatus;
import com.myshop.domain.enums.PaymentGateway;
import com.myshop.domain.enums.PaymentStatus;
import com.myshop.dto.request.CheckoutRequest;
import com.myshop.dto.request.OrderStatusUpdateRequest;
import com.myshop.dto.response.OrderResponse;
import com.myshop.mapper.DtoMapper;
import com.myshop.repository.CartItemRepository;
import com.myshop.repository.OrderRepository;
import com.myshop.repository.PaymentRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.repository.UserRepository;
import com.myshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .orderStatus(OrderStatus.PROCESSING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .shippingName(request.getShippingName())
                .shippingAddress(request.getShippingAddress())
                .shippingPhone(request.getShippingPhone())
                .shippingEmail(request.getShippingEmail())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found during checkout"));
            int requestedQty = cartItem.getQuantity();
            if (requestedQty <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity in cart");
            }
            if (requestedQty > product.getStockQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - requestedQty);
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestedQty)
                    .unitPrice(product.getPrice())
                    .build();
            orderItems.add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(requestedQty)));
        }

        if (orderItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        if (request.getPaymentMethod() == OrderPaymentMethod.ONLINE_PAYMENT) {
            PaymentGateway gateway = request.getPaymentGateway();
            if (gateway == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment gateway is required for online payments");
            }
            Payment payment = Payment.builder()
                    .order(savedOrder)
                    .paymentMethod(gateway)
                    .paymentStatus(PaymentStatus.PENDING)
                    .amount(total)
                    .build();
            paymentRepository.save(payment);
            savedOrder.setPayment(payment);
        }

        // clear cart
        cartItems.forEach(cartItemRepository::delete);

        return DtoMapper.toOrderResponse(savedOrder, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return DtoMapper.toOrderResponse(order, order.getOrderItems());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return orderRepository.findByUserId(userId).stream()
                .map(order -> DtoMapper.toOrderResponse(order, order.getOrderItems()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> DtoMapper.toOrderResponse(order, order.getOrderItems()))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getOrderStatus() == OrderStatus.DELIVERED
                && request.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivered orders cannot revert to previous status");
        }

        // --- DEBUT MODIFICATION : Remise en stock si Annulation ---
        if (request.getOrderStatus() == OrderStatus.CANCELLED && order.getOrderStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                int newStock = product.getStockQuantity() + item.getQuantity();
                product.setStockQuantity(newStock);
                productRepository.save(product);
            }
        }
        // --- FIN MODIFICATION ---

        order.setOrderStatus(request.getOrderStatus());

        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
            Payment payment = order.getPayment();
            if (payment != null) {
                payment.setPaymentStatus(request.getPaymentStatus());
                paymentRepository.save(payment);
            }
        }

        Order saved = orderRepository.save(order);
        return DtoMapper.toOrderResponse(saved, saved.getOrderItems());
    }
}