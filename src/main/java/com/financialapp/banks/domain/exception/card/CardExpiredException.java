package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class CardExpiredException extends DomainException {
    public CardExpiredException(String cardNumber, String expiryDate) {
        super(DomainError.CARD_EXPIRED,
              "Card '" + cardNumber + "' expiry date cannot be in the past",
              Map.of("cardNumber", cardNumber, "expiryDate", expiryDate));
    }
}
