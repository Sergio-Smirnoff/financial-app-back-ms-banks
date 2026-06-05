package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class CardInstallmentNotSupportedException extends DomainException {
    public CardInstallmentNotSupportedException(String cardNumber) {
        super(DomainError.CARD_INSTALLMENT_NOT_SUPPORTED,
              "Card '" + cardNumber + "' does not support installment-based expenses",
              Map.of("cardNumber", cardNumber));
    }
}
