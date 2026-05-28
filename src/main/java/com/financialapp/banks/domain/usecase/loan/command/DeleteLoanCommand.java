package com.financialapp.banks.domain.usecase.loan.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.loan.LoanId;

public record DeleteLoanCommand(LoanId id, UserId userId) {}
