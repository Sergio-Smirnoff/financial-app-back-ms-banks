package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidCardBillingException;

public record CardBilling(int closingDay, int dueDay) {
    public CardBilling {
        if (closingDay < 1 || closingDay > 31) {
            throw new InvalidCardBillingException("closingDay must be between 1 and 31, got " + closingDay);
        }
        if (dueDay < 1 || dueDay > 31) {
            throw new InvalidCardBillingException("dueDay must be between 1 and 31, got " + dueDay);
        }
    }
}
