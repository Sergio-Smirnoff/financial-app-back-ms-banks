package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Loan(
    LoanId id,
    UserId userId,
    BankName bankName,
    String name,
    Money principal,
    BigDecimal interestRate,
    int totalInstallments,
    int remainingInstallments,
    AmortizationType amortizationType,
    LocalDate startDate,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
