package com.financialapp.banks.domain.model.loan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.financialapp.banks.domain.model.account.Account;

public record LoanRequest(
    LoanRequestId id,
    Loan Loan,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDate startDate,
    Account depositAccount // User / Bank
) {}
