package com.financialapp.banks.domain.usecase.loan;

import com.financialapp.banks.domain.usecase.loan.command.CreateLoanCommand;
import com.financialapp.banks.domain.model.loan.Loan;

public interface CreateLoanUseCase {
    Loan execute(CreateLoanCommand command);
}
