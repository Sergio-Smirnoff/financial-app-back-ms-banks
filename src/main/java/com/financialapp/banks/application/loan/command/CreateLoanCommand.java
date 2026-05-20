package com.financialapp.banks.application.loan.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.bank.BankId;
import com.financialapp.banks.domain.model.loan.AmortizationType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanCommand(
    UserId userId,
    BankId bankId,
    AccountId destinationAccountId,
    String name,
    BigDecimal principal,
    BigDecimal interestRate,
    int totalInstallments,
    LocalDate startDate,
    AmortizationType amortizationType
) {}
