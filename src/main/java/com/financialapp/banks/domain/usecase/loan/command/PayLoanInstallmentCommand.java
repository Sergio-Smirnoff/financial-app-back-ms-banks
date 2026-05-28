package com.financialapp.banks.domain.usecase.loan.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;

import java.time.LocalDate;

public record PayLoanInstallmentCommand(
    LoanId loanId,
    LoanInstallmentId installmentId,
    UserId userId,
    String accountCbu,
    LocalDate paidDate
) {}
