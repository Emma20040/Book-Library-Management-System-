package com.library.management_system.controllers;

import com.library.management_system.services.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
//@RequestMapping("/api")
@Slf4j
public class StripeWebhookController {
    private final PaymentService paymentService;
    private final String webhookSecret;

    public StripeWebhookController(
            PaymentService paymentService,
            @Value("${stripe.webhook.secret}") String webhookSecret) {
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook/payment")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("Received webhook with signature: {}", sigHeader);
        log.info("Payload: {}", payload);

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            paymentService.capturePayment(event);


            // Always return 200 so Stripe knows you accepted it
            return ResponseEntity.ok("Webhook received");
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook signature");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing webhook");
        }
    }
//    public void handleWebhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) {
//        try {
//            String payload = request.getReader().lines().collect(Collectors.joining("\n"));
//            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
//            paymentService.capturePayment(event);
//        } catch (SignatureVerificationException e) {
//            log.error("Webhook signature verification failed", e);
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid webhook signature");
//        } catch (Exception e) {
//            log.error("Error processing webhook", e);
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing webhook");
//        }
//    }

    @PostMapping("/webhook/test")
    public ResponseEntity<String> testWebhook(@RequestBody(required = false) String body) {
        log.info("Test webhook reached - Application is accessible");
        return ResponseEntity.ok("Webhook endpoint is reachable - " + LocalDateTime.now());
    }
}
//stripe listen --forward-to localhost:8080/webhook/payment
