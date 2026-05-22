package com.financialapp.banks.domain.model.card.cardType;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardDetails;

import java.time.LocalDateTime;

public class DebitCard extends Card {

    public DebitCard(String cardNumber, UserId userId, BankName bankName,
                     CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(cardNumber, userId, bankName, details, createdAt, updatedAt);
    }
}
