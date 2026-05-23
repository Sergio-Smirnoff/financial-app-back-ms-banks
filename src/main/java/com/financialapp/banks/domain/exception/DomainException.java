package com.financialapp.banks.domain.exception;

import java.util.Map;

public abstract class DomainException extends RuntimeException {

    private final DomainError error;
    private final Map<String, Object> details;

    protected DomainException(DomainError error, String message) {
        super(message);
        this.error = error;
        this.details = null;
    }

    protected DomainException(DomainError error, String message, Map<String, Object> details) {
        super(message);
        this.error = error;
        this.details = details;
    }

    public DomainError getError() { return error; }
    public Map<String, Object> getDetails() { return details; }
}
