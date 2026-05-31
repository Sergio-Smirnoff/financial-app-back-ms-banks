package com.financialapp.banks.domain.exception.cbu;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class InvalidCbuException extends DomainException {
    public InvalidCbuException(String value, String reason) {
        super(DomainError.INVALID_CBU,
              "Invalid CBU: " + reason,
              Map.of("cbu", value == null ? "null" : value));
    }
}
