package com.financialapp.banks.domain.exception.loan;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class LoanInstallmentAlreadyPaidException extends DomainException {
    public LoanInstallmentAlreadyPaidException(String installmentId) {
        super(DomainError.LOAN_INSTALLMENT_ALREADY_PAID,
              "Loan installment '" + installmentId + "' is already paid",
              Map.of("installmentId", installmentId));
    }
}
