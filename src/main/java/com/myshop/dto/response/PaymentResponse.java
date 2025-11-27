package com.myshop.dto.response;

import com.myshop.domain.enums.PaymentGateway;
import com.myshop.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class PaymentResponse {
    Long id;
    PaymentGateway paymentMethod;
    PaymentStatus paymentStatus;
    BigDecimal amount;
    String transactionReference;
    Instant paymentDate;
}

