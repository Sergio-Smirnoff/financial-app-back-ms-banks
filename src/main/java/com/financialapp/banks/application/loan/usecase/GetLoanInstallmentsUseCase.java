package com.financialapp.banks.application.loan.usecase;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;

import java.util.List;

public interface GetLoanInstallmentsUseCase {
    List<LoanInstallment> execute(LoanId loanId, UserId userId);
}
