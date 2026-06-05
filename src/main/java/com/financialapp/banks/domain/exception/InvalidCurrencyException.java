package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class InvalidCurrencyException extends DomainException {
    public InvalidCurrencyException(String value) {
        super(DomainError.INVALID_CURRENCY,
              "Invalid currency code: '" + value + "'. Expected a valid ISO 4217 code (e.g. ARS, USD).",
              Map.of("value", String.valueOf(value)));
    }
}
