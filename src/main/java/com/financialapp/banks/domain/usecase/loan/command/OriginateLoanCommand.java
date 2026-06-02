package com.financialapp.banks.domain.usecase.loan.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;

import java.time.LocalDate;

public record OriginateLoanCommand(
    UserId userId,
    BankNumber bankNumber,
    String destinationAccountCbu,
    String name,
    String principal,
    String interestRate,
    int totalInstallments,
    LocalDate startDate,
    AmortizationType amortizationType
) {}
