package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LoanResponse(
        Long id,
        String bankNumber,
        Long userId,
        String name,
        String principal,
        String currency,
        String interestRate,
        int totalInstallments,
        int remainingInstallments,
        LocalDate startDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
