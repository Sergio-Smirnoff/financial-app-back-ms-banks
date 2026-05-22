package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CardInstallmentResponse(
        Long id,
        String cardNumber,
        String description,
        BigDecimal totalAmount,
        String currency,
        int installmentNumber,
        int totalInstallments,
        BigDecimal amount,
        LocalDate dueDate,
        boolean paid,
        LocalDate paidDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
