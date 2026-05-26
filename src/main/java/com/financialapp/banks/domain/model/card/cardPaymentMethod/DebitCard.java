package com.financialapp.banks.domain.model.card.cardPaymentMethod;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardNumber;

import java.time.LocalDateTime;

public class DebitCard extends Card {

    public DebitCard(CardNumber cardNumber, UserId userId, BankName bankName,
                     CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(cardNumber, userId, bankName, details, createdAt, updatedAt);
    }
}
