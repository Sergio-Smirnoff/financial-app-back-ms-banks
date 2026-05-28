package com.financialapp.banks.domain.usecase.loan;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.Loan;

import java.util.List;

public interface ListLoansUseCase {
    List<Loan> execute(UserId userId, BankName bankName);
}
