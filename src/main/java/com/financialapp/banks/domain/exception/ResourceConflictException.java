package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class ResourceConflictException extends DomainException {
    public ResourceConflictException(DomainError error, String message) {
        super(error, message);
    }

    public ResourceConflictException(DomainError error, String message, Map<String, Object> details) {
        super(error, message, details);
    }
}
