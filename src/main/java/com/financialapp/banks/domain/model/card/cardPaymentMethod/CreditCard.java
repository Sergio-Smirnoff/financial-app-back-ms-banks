package com.financialapp.banks.domain.model.card.cardPaymentMethod;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardNumber;

import java.time.LocalDateTime;

public class CreditCard extends Card {

    public CreditCard(CardNumber cardNumber, UserId userId, BankNumber bankNumber,
                      CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(cardNumber, userId, bankNumber, details, createdAt, updatedAt);
    }
}
