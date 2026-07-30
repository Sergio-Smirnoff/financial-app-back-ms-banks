package com.financialapp.banks.domain.exception.fee;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class InvalidFeeScheduleException extends DomainException {
    public InvalidFeeScheduleException(String message) {
        super(DomainError.INVALID_FEE_SCHEDULE, message, Map.of());
    }
}
