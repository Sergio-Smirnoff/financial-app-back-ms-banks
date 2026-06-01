package com.financialapp.banks.domain.exception;

import java.util.Map;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(DomainError.RESOURCE_NOT_FOUND,
              resourceType + " '" + identifier + "' not found",
              Map.of("resourceType", resourceType, "identifier", identifier));
    }
}
