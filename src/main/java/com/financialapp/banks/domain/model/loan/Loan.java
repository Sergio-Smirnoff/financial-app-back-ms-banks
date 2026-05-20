package com.financialapp.banks.domain.model.loan;


import java.math.BigDecimal;
import com.financialapp.banks.domain.common.model.Money;

public record Loan(
    LoanId id,
    String description,
    Money principal,
    BigDecimal interestRate,
    int totalInstallments,
    AmortizationType amortizationType
) {}

