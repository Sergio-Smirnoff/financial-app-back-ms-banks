package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class ResourceAlreadyExistsException extends DomainException {
    public ResourceAlreadyExistsException(String resourceType, String identifier) {
        super(DomainError.RESOURCE_ALREADY_EXISTS,
              resourceType + " '" + identifier + "' already exists",
              Map.of("resourceType", resourceType, "identifier", identifier));
    }
}
