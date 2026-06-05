package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.DomainException;

public class InfrastructureException extends DomainException {
    public InfrastructureException(String message) {
        super(DomainError.INTERNAL_ERROR, message);
    }
}
