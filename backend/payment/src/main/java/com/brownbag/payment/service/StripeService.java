package com.brownbag.payment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.brownbag.payment.dto.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class StripeService {
    
    @Value("${STRIPE_SECRET_KEY}")
    private String SECRET_KEY;
    
    
    private ArrayList<Product> getDummyProducts() {
        ArrayList<Product> products = new ArrayList<Product>();
        
        products.add(Product.builder().id(1).amount(75.00).build());
        products.add(Product.builder().id(2).amount(35.00).build());
        products.add(Product.builder().id(3).amount(27.00).build());
        products.add(Product.builder().id(4).amount(149.00).build());
        
        return products;
    }
    
    private double getProductAmount(int id) {
        ArrayList<Product> products = getDummyProducts();
        
        for(Product product:products) {
            if(product.id == id)
                return product.amount;
        }
        
        return 0.00;
    }
    
    private Long getLongAmount(double amount) {
        Long longTotal = Math.round(amount * 100); 
        
        return longTotal;
    }
    
    public Double getDoubleAmount(Long amount) {
        Double total = (double) (amount / 100); 
        
        return total;
    }
    
    public PaymentIntent createPaymentIntent(int productId) throws StripeException {
        Stripe.apiKey = SECRET_KEY;
        
        double amount = getProductAmount(productId);
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams
            .builder()
            .setAmount(getLongAmount(amount))
            .setCurrency("usd")
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods
                    .builder()
                    .setEnabled(true)
                    .build()
            )
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        
        return paymentIntent;
    }
}
