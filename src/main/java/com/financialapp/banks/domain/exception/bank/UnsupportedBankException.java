package com.financialapp.banks.domain.exception.bank;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;

import java.util.Map;

public class UnsupportedBankException extends DomainException {
    public UnsupportedBankException(String value) {
        super(DomainError.UNSUPPORTED_BANK,
              "Unsupported bank: '" + value + "'",
              Map.of("value", value));
    }
}
