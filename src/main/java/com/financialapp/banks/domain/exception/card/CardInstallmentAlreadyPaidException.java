package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class CardInstallmentAlreadyPaidException extends DomainException {
    public CardInstallmentAlreadyPaidException(String installmentId) {
        super(DomainError.CARD_INSTALLMENT_ALREADY_PAID,
              "Installment '" + installmentId + "' is already paid",
              Map.of("installmentId", installmentId));
    }
}
