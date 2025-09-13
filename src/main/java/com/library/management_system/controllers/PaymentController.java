package com.library.management_system.controllers;

import com.library.management_system.DTOs.PaymentRequestDto;
import com.library.management_system.DTOs.PaymentResponseDto;
import com.library.management_system.DTOs.TransactionResponseDto;
import com.library.management_system.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDto> initiatePayment(@RequestBody PaymentRequestDto request) {
        PaymentResponseDto response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionHistory() {
        List<TransactionResponseDto> transactions = paymentService.getTransactionHistory();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/access/{bookId}")
    public ResponseEntity<Boolean> checkBookAccess(@PathVariable Long bookId) {
        boolean hasAccess = paymentService.hasAccessToBook(bookId);
        return new ResponseEntity<>(hasAccess, HttpStatus.OK);
    }
}
