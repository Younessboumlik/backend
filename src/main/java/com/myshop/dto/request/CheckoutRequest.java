package com.myshop.dto.request;

import com.myshop.domain.enums.OrderPaymentMethod;
import com.myshop.domain.enums.PaymentGateway;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 150)
    private String shippingName;

    @NotBlank
    @Size(max = 255)
    private String shippingAddress;

    @NotBlank
    @Size(max = 20)
    private String shippingPhone;

    @NotBlank
    @Email
    @Size(max = 100)
    private String shippingEmail;

    @NotNull
    private OrderPaymentMethod paymentMethod;

    private PaymentGateway paymentGateway;
}

