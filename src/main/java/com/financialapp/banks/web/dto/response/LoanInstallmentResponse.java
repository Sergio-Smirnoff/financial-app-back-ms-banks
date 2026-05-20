package com.financialapp.banks.model.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LoanInstallmentResponse(
        Long id,
        Long loanId,
        int installmentNumber,
        BigDecimal amount,
        LocalDate dueDate,
        boolean paid,
        LocalDate paidDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
