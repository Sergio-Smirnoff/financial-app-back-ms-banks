package com.financialapp.banks.domain.exception.loan;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class LoanInstallmentMismatchException extends DomainException {
    public LoanInstallmentMismatchException(String installmentId, String loanId) {
        super(DomainError.LOAN_INSTALLMENT_MISMATCH,
              "Installment '" + installmentId + "' does not belong to loan '" + loanId + "'",
              Map.of("installmentId", installmentId, "loanId", loanId));
    }
}
