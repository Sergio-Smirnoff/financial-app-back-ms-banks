package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanInstallment(
    LoanInstallmentId id,
    LoanId loanId,
    int installmentNumber,
    Money amount,
    LocalDate dueDate,
    boolean paid,
    LocalDate paidDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
