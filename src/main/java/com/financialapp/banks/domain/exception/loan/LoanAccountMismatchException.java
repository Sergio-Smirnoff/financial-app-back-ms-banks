package com.financialapp.banks.domain.exception.loan;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class LoanAccountMismatchException extends DomainException {
    public LoanAccountMismatchException(String accountCbu, String bankName) {
        super(DomainError.LOAN_ACCOUNT_MISMATCH,
              "Account '" + accountCbu + "' does not belong to bank '" + bankName + "'",
              Map.of("accountCbu", accountCbu, "bankName", bankName));
    }
}
