package com.financialapp.banks.application.loan.usecase;

import com.financialapp.banks.application.loan.command.PayLoanInstallmentCommand;
import com.financialapp.banks.domain.model.loan.LoanInstallment;

public interface PayLoanInstallmentUseCase {
    LoanInstallment execute(PayLoanInstallmentCommand command);
}
