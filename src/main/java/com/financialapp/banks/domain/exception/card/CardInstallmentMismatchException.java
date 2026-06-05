package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class CardInstallmentMismatchException extends DomainException {
    public CardInstallmentMismatchException(String installmentId, String cardNumber) {
        super(DomainError.CARD_INSTALLMENT_MISMATCH,
              "Installment '" + installmentId + "' does not belong to card '" + cardNumber + "'",
              Map.of("installmentId", installmentId, "cardNumber", cardNumber));
    }
}
