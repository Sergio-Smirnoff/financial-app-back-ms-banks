package com.financialapp.banks.application.loan.usecase;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;

public interface GetLoanUseCase {
    Loan execute(LoanId id, UserId userId);
}
