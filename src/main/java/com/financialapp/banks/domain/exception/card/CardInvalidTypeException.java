package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class CardInvalidTypeException extends DomainException {
    public CardInvalidTypeException(String subtype) {
        super(DomainError.CARD_INVALID_TYPE,
              "Unknown card type: '" + subtype + "'",
              Map.of("subtype", subtype));
    }
}
