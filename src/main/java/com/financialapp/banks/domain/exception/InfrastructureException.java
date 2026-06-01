package com.financialapp.banks.domain.exception;

public class InfrastructureException extends DomainException {
    public InfrastructureException(String message) {
        super(DomainError.INTERNAL_ERROR, message);
    }
}
