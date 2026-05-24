package com.financialapp.banks.infrastructure.exception;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class FinancesServiceException extends DomainException {
    public FinancesServiceException(String operation, String cause) {
        super(DomainError.FINANCES_SERVICE_UNAVAILABLE,
              "ms-finances unavailable during operation '" + operation + "'",
              Map.of("operation", operation, "cause", cause != null ? cause : "unknown"));
    }
}
