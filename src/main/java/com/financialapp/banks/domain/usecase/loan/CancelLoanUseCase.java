package com.financialapp.banks.domain.usecase.loan;

import com.financialapp.banks.domain.usecase.loan.command.CancelLoanCommand;

public interface CancelLoanUseCase {
    void execute(CancelLoanCommand command);
}
