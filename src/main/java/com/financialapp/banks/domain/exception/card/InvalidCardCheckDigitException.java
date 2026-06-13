package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class InvalidCardCheckDigitException extends DomainException {
    public InvalidCardCheckDigitException(String value) {
        super(DomainError.INVALID_CARD_CHECK_DIGIT,
              "Card number check digit is invalid (Luhn). Submit 15 digits to auto-complete it.",
              Map.of("cardNumber", value == null ? "null" : value));
    }
}
