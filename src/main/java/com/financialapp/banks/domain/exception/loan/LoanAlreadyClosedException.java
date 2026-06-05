package com.financialapp.banks.domain.exception.loan;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class LoanAlreadyClosedException extends DomainException {
    public LoanAlreadyClosedException(String loanId) {
        super(DomainError.LOAN_ALREADY_CLOSED,
              "Loan '" + loanId + "' is already closed",
              Map.of("loanId", loanId));
    }
}
