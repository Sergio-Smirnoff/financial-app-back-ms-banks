package com.financialapp.banks.domain.usecase.loan.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.AmortizationType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanCommand(
    UserId userId,
    BankName bankName,
    String destinationAccountCbu,
    String name,
    BigDecimal principal,
    BigDecimal interestRate,
    int totalInstallments,
    LocalDate startDate,
    AmortizationType amortizationType
) {}
