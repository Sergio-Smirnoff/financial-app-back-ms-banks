package com.financialapp.banks.domain.exception.snapshot;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class InvalidBalanceSnapshotException extends DomainException {
    public InvalidBalanceSnapshotException(String message) {
        super(DomainError.INVALID_BALANCE_SNAPSHOT, message, Map.of());
    }
}
