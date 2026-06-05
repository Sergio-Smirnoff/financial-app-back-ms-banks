package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class InvalidAccountNumberException extends DomainException {
    public InvalidAccountNumberException(String value) {
        super(DomainError.INVALID_ACCOUNT_NUMBER,
              "Account number must be exactly 13 digits",
              Map.of("accountNumber", value == null ? "null" : value));
    }
}
