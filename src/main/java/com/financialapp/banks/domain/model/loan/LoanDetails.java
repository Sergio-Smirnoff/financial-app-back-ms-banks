package com.financialapp.banks.domain.model.loan;

import java.math.BigDecimal;

import com.financialapp.banks.domain.common.model.Money;

public record LoanDetails(
    Money principal,
    BigDecimal interestRate,
    int totalInstallments,
    AmortizationType amortizationType
) {}
