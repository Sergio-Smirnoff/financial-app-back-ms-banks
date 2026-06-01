package com.financialapp.banks.web.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LoanInstallmentResponse(
        Long id,
        Long loanId,
        int installmentNumber,
        String amount,
        LocalDate dueDate,
        boolean paid,
        LocalDate paidDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
