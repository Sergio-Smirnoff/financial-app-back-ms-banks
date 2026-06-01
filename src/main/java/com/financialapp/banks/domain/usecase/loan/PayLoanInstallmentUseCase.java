package com.financialapp.banks.domain.usecase.loan;

import com.financialapp.banks.domain.usecase.loan.command.PayLoanInstallmentCommand;
import com.financialapp.banks.domain.model.loan.LoanInstallment;

public interface PayLoanInstallmentUseCase {
    LoanInstallment execute(PayLoanInstallmentCommand command);
}
