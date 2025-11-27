package com.myshop.dto.request;

import com.myshop.domain.enums.OrderStatus;
import com.myshop.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;
}

