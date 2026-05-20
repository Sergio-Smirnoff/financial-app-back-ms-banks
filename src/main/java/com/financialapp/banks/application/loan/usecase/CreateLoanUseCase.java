package com.financialapp.banks.application.loan.usecase;

import com.financialapp.banks.application.loan.command.CreateLoanCommand;
import com.financialapp.banks.domain.model.loan.Loan;

public interface CreateLoanUseCase {
    Loan execute(CreateLoanCommand command);
}
