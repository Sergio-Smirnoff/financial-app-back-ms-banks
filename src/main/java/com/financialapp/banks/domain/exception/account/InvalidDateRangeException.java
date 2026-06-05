package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class InvalidDateRangeException extends DomainException {
    public InvalidDateRangeException(String from, String to) {
        super(DomainError.INVALID_DATE_RANGE,
              "'from' date must not be after 'to' date",
              Map.of("from", from, "to", to));
    }
}
