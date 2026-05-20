package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LoanResponse(
        Long id,
        String bankName,
        Long userId,
        String name,
        BigDecimal principal,
        String currency,
        BigDecimal interestRate,
        int totalInstallments,
        int remainingInstallments,
        LocalDate startDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
