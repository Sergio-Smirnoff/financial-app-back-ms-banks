package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class InvestmentsServiceException extends DomainException {

    public InvestmentsServiceException(String operation, String cause) {
        super(DomainError.INVESTMENTS_SERVICE_UNAVAILABLE,
              "ms-investments unavailable during operation '" + operation + "'",
              Map.of("operation", operation, "cause", cause != null ? cause : "unknown"));
    }
}
