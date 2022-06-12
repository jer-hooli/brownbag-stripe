package com.brownbag.payment.controller;

import com.brownbag.payment.service.StripeService;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stripe")
public class WebhookController {
    
    @Value("${STRIPE_SECRET_KEY}")
    private String SECRET_KEY;
    
    @Value("${STRIPE_WEBHOOOK_SECRET_KEY}")
    private String whsec;
    
    @Autowired
    private StripeService stripeService;
    
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
        @RequestBody String request, 
        @RequestHeader("Stripe-Signature") String sigHeader) {
            
        Stripe.apiKey = SECRET_KEY;
        // If you are testing your webhook locally with the Stripe CLI you
        // can find the endpoint's secret by running `stripe listen`
        // Otherwise, find your endpoint's secret in your webhook settings in the Developer Dashboard
        String endpointSecret = whsec;
        String payload = request;
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            return new ResponseEntity<String>("Invalid Payload", HttpStatus.BAD_REQUEST);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            return new ResponseEntity<String>("Invalid Stripe Signature Header", HttpStatus.BAD_REQUEST);
        }

        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
            return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded": {
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                
                System.out.println("This amount has been paid: $" + stripeService.getDoubleAmount(paymentIntent.getAmount()));
                
                break;
            }

            case "invoice.payment_succeeded": { 
                break;
            }

            case "invoice.upcoming": {
                break;
            }

            case "invoice.paid":
                // Used to provision services after the trial has ended.
                // The status of the invoice will show up as paid. Store the status in your
                // database to reference when a user accesses your service to avoid hitting rate
                // limits.
                break;
            case "invoice.payment_failed": {
                // If the payment fails or the customer does not have a valid payment method,
                // an invoice.payment_failed event is sent, the subscription becomes past_due.
                // Use this webhook to notify your user that their payment has
                // failed and to retrieve new card details.

                break;
            }
            case "invoice.finalized":
                // If you want to manually send out invoices to your customers
                // or store them locally to reference to avoid hitting Stripe rate limits.
                break;

            case "customer.subscription.created": {
                break;
            }

            case "customer.subscription.updated": {
                break;
            }

            case "customer.subscription.deleted": {
                break;
            }

            case "customer.subscription.trial_will_end": {
                break;
            }

            case "payment_method.attached": {
                break;
            }

            // Handle other event types

            default:
                System.out.println("Unhandled event type: " + event.getType());
        }

        return new ResponseEntity<String>("", HttpStatus.OK);
    }
}
