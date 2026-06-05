package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class FinancesServiceException extends DomainException {
    public FinancesServiceException(String operation, String cause) {
        super(DomainError.FINANCES_SERVICE_UNAVAILABLE,
              "ms-finances unavailable during operation '" + operation + "'",
              Map.of("operation", operation, "cause", cause != null ? cause : "unknown"));
    }
}
