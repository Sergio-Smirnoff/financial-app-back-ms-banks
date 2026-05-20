package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Loan(
    LoanId id,
    UserId userId,
    BankName bankName,
    String name,
    LoanDetails details,
    int remainingInstallments,
    LocalDate startDate,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}


