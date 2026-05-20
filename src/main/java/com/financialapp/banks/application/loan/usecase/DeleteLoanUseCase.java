package com.financialapp.banks.application.loan.usecase;

import com.financialapp.banks.application.loan.command.DeleteLoanCommand;

public interface DeleteLoanUseCase {
    void execute(DeleteLoanCommand command);
}
