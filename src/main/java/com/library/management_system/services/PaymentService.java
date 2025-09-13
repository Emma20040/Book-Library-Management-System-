package com.library.management_system.services;

import com.library.management_system.DTOs.PaymentRequestDto;
import com.library.management_system.DTOs.PaymentResponseDto;
import com.library.management_system.DTOs.TransactionResponseDto;
import com.library.management_system.enums.PaymentStatus;
import com.library.management_system.models.Book;
import com.library.management_system.models.BookAccess;
import com.library.management_system.models.Transaction;
import com.library.management_system.models.UserModel;
import com.library.management_system.repositories.BookAccessRepository;
import com.library.management_system.repositories.BookRepository;
import com.library.management_system.repositories.TransactionRepository;
import com.library.management_system.repositories.UserRepository;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.checkout.Session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PaymentService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BookAccessRepository bookAccessRepository;
    private final EmailService emailService;
    private final String frontendUrl;

    public PaymentService(
            BookRepository bookRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            BookAccessRepository bookAccessRepository,
            EmailService emailService,
            @Value("${frontend.url}") String frontendUrl) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.bookAccessRepository = bookAccessRepository;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

//    Get the current user
    private UserModel getCurrentUser() {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID format");
    }
    }

    @Transactional
    public PaymentResponseDto initiatePayment(PaymentRequestDto request) {
        UserModel user = getCurrentUser();
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with ID: " + request.getBookId()));

        // Check for duplicate purchase
        LocalDateTime currentDate = LocalDateTime.now();
        bookAccessRepository.findActiveAccess(user, book, currentDate)
                .ifPresent(access -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Active access exists for book ID: " + book.getId() + " until " + access.getEndDate()
                    );
                });

        // Calculate payment amount
        if (request.getDurationDays() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        BigDecimal dailyRate = book.getPricePerMonth().divide(
                BigDecimal.valueOf(30),
                new MathContext(10, RoundingMode.HALF_UP)
        );

        BigDecimal amount = dailyRate.multiply(BigDecimal.valueOf(request.getDurationDays()))
                .setScale(2, RoundingMode.HALF_UP);
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();


        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setAmount(amount);
        transaction.setCurrency("USD");
        transaction.setDurationDays(request.getDurationDays());
        transaction.setPaymentStatus(PaymentStatus.PENDING);
        transaction.setTransactionDate(currentDate);


        // Create Stripe Checkout Session
        try {
            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(user.getEmail())
                    .setSuccessUrl(frontendUrl + "/payment/success")
                    .setCancelUrl(frontendUrl + "/payment/failure")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(book.getTitle())
                                                                    .setDescription("Access for " + request.getDurationDays() + " days")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("user_id", user.getId().toString())
                    .putMetadata("book_id", String.valueOf(book.getId()))
                    .putMetadata("duration_days", String.valueOf(request.getDurationDays()))
                    .build();

            Session session = Session.create(sessionParams);
            transaction.setStripePaymentIntentId(session.getId());
            transactionRepository.save(transaction);

            return new PaymentResponseDto(session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Checkout Session", e);
            throw new RuntimeException("Failed to initiate payment", e);
        }
    }

//    the mail which will be sent after payment
    private void sendPaymentConfirmationEmail(Transaction transaction, BookAccess access) {
//        time formatter to reduce precsion
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String to = transaction.getUser().getEmail();
        String subject = "Payment Confirmation for Book Access";
        String text = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "    .email-container { max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border: 1px solid #ddd; }\n" +
                "    pre { white-space: pre-wrap; word-wrap: break-word; font-family: inherit; font-size: 14px; color: #333; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class='email-container'>\n" +
                "    <pre>" +
                "Dear " + transaction.getUser().getUsername() + ",\n\n" +
                "We are pleased to confirm that your payment has been successfully processed. " +
                "Your digital access to the requested content has been activated.\n\n" +
                "ORDER SUMMARY:\n" +
                "\n" +
                "• Publication: " + transaction.getBook().getTitle() + "\n" +
                "• Transaction Reference: " + transaction.getId() + "\n" +
                "• Payment Amount: " + transaction.getAmount() + " " + transaction.getCurrency() + "\n" +
                "• Processed On: " + transaction.getTransactionDate().format(dateFormatter) + "\n\n" +
                "ACCESS DETAILS:\n" +
                "\n" +
                "• Access Period: " + access.getDurationDays() + " days\n" +
                "• Start Date: " + access.getStartDate().format(dateFormatter) + "\n" +
                "• Expiration Date: " + access.getEndDate().format(dateFormatter) + "\n\n" +
                "You may access your content at any time through our digital portal:\n" +
                frontendUrl + "\n\n" +
                "For technical support or questions regarding your access, please contact our " +
                "customer service team or visit our help center.\n\n" +
                "Thank you for choosing our digital library services.\n\n" +
                "Sincerely,\n" +
                "The Digital Library Team\n" +
                "ReadQuest\n\n" +
                "---\n" +
                "This is an automated message. Please do not reply to this email." +
                "</pre>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";


        try {
            emailService.sendHtmlEmail(to, subject, text);
            log.info("Payment confirmation email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email", e);
        }
    }


    @Transactional
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Transaction transaction = transactionRepository.findByStripePaymentIntentId(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found for session ID: " + sessionId));

            transaction.setPaymentStatus(PaymentStatus.PAID);
            transactionRepository.save(transaction);
            transactionRepository.flush(); // Force immediate database write, this might solve the issue of some payments showning PENDING instead of PAID

            BookAccess access = new BookAccess();
            access.setUser(transaction.getUser());
            access.setBook(transaction.getBook());
            access.setDurationDays(transaction.getDurationDays());
            access.setStartDate(LocalDateTime.now());
            access.setEndDate(access.getStartDate().plusDays(transaction.getDurationDays()));
            access.setTransaction(transaction);
            bookAccessRepository.save(access);

            sendPaymentConfirmationEmail(transaction, access);
            log.info("Successfully processed payment for transaction ID: {}", transaction.getId());
        } else if ("checkout.session.expired".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Transaction transaction = transactionRepository.findByStripePaymentIntentId(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found for session ID: " + sessionId));

            transaction.setPaymentStatus(PaymentStatus.FAILED);
            transactionRepository.save(transaction);
            log.info("Payment failed for transaction ID: {}", transaction.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionHistory() {
        UserModel user = getCurrentUser();
        return transactionRepository.findByUser(user).stream().map(transaction -> {
            TransactionResponseDto dto = new TransactionResponseDto();
            dto.setTransactionId(transaction.getId());
            dto.setBookTitle(transaction.getBook().getTitle());
            dto.setAmount(transaction.getAmount());
            dto.setCurrency(transaction.getCurrency());
            dto.setDurationDays(transaction.getDurationDays());
            dto.setPaymentStatus(transaction.getPaymentStatus());
            dto.setTransactionDate(transaction.getTransactionDate());
            bookAccessRepository.findActiveAccess(
                    transaction.getUser(),
                    transaction.getBook(),
//                    transaction.getTransactionDate()
                    LocalDateTime.now()
            ).ifPresent(access -> {
                dto.setStartDate(access.getStartDate());
                dto.setEndDate(access.getEndDate());
            });
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasAccessToBook(Long bookId) {
        UserModel user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with ID: " + bookId));
        return bookAccessRepository.findActiveAccess(user, book, LocalDateTime.now()).isPresent();
    }


}
