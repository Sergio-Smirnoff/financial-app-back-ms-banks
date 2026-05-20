package com.financialapp.banks.domain.model.loan;

import java.math.BigDecimal;

public record LoanDetails(
    BigDecimal principal,
    String currency,
    BigDecimal interestRate,
    int totalInstallments,
    AmortizationType amortizationType
) {}
