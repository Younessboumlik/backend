package com.myshop.controller;

import com.myshop.dto.request.PaymentRequest;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.myshop.config.StripConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private StripConfig stripeConfig;

    // Endpoint pour créer un paiement
    @PostMapping("/create-payment-intent")
    public PaymentIntent createPaymentIntent(@RequestBody PaymentRequest paymentRequest) {
        try {
            // Création d'une PaymentIntent avec le montant reçu
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount() * 100) // Stripe prend en charge les montants en cents
                    .setCurrency("mad")  // Utilise la devise MAD
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentIntent;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création du paiement Stripe.");
        }
    }
}
