package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;

import java.util.Map;

public class InvalidCardBillingException extends DomainException {
    public InvalidCardBillingException(String message) {
        super(DomainError.INVALID_CARD_BILLING, message, Map.of());
    }
}
