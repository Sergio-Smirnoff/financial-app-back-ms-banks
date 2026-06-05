package com.financialapp.banks.domain.exception.bank;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class InvalidBankNumberException extends DomainException {
    public InvalidBankNumberException(String value) {
        super(DomainError.INVALID_BANK_NUMBER,
              "Bank number must be exactly 3 digits",
              Map.of("bankNumber", value == null ? "null" : value));
    }
}
