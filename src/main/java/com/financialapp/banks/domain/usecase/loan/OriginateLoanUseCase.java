package com.financialapp.banks.domain.usecase.loan;

import com.financialapp.banks.domain.usecase.loan.command.OriginateLoanCommand;
import com.financialapp.banks.domain.model.loan.Loan;

public interface OriginateLoanUseCase {
    Loan execute(OriginateLoanCommand command);
}
