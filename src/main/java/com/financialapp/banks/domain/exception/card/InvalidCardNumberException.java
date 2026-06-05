package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class InvalidCardNumberException extends DomainException {
    public InvalidCardNumberException(String value) {
        super(DomainError.INVALID_CARD_NUMBER,
              "Card number must be exactly 16 digits",
              Map.of("cardNumber", value == null ? "null" : value));
    }
}
