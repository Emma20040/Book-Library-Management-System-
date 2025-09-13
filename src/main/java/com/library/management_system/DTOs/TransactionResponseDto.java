package com.library.management_system.DTOs;

import com.library.management_system.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {
    private UUID transactionId;
    private String bookTitle;
    private BigDecimal amount;
    private String currency;
    private Integer durationDays;
    private PaymentStatus paymentStatus;
    private LocalDateTime transactionDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
