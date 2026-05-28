package com.financialapp.banks.domain.usecase.loan;

import com.financialapp.banks.domain.usecase.loan.command.DeleteLoanCommand;

public interface DeleteLoanUseCase {
    void execute(DeleteLoanCommand command);
}
