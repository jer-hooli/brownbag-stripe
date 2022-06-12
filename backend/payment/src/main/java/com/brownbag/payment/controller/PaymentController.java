package com.brownbag.payment.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.brownbag.payment.dto.CreatePaymentIntentRequest;
import com.brownbag.payment.exception.ApiError;
import com.brownbag.payment.service.StripeService;
import com.stripe.model.PaymentIntent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    
    @Autowired
    private StripeService stripeService;

    
    @PostMapping("/payment-intent")
    public ResponseEntity<?> createSetupIntent(@RequestBody CreatePaymentIntentRequest request) {
        try {
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(request.getProductId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        }
        catch(Exception e) {
            ApiError apiError = ApiError.builder()
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        
            return new ResponseEntity<ApiError>(apiError, HttpStatus.BAD_REQUEST);
        }
    }    
}
