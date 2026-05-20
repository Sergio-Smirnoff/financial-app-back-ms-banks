package com.financialapp.banks.application.loan.usecase;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.Loan;

import java.util.List;

public interface ListLoansUseCase {
    List<Loan> execute(UserId userId, BankName bankName);
}
