package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;

import java.time.LocalDateTime;

public abstract class Card {

    protected final CardNumber cardNumber;
    protected final UserId userId;
    protected final BankNumber bankNumber;
    protected final CardDetails details;
    protected final LocalDateTime createdAt;
    protected final LocalDateTime updatedAt;

    protected Card(CardNumber cardNumber, UserId userId, BankNumber bankNumber,
                   CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cardNumber = cardNumber;
        this.userId = userId;
        this.bankNumber = bankNumber;
        this.details = details;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Card create(String number, UserId userId, BankNumber bankNumber,
                              CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        CardNumber cardNumber = CardNumber.from(number);
        return details.behavior() == CardBehavior.INSTANT_PAYMENT
                ? new DebitCard(cardNumber, userId, bankNumber, details, createdAt, updatedAt)
                : new CreditCard(cardNumber, userId, bankNumber, details, createdAt, updatedAt);
    }

    public CardNumber cardNumber() { return cardNumber; }
    public UserId userId() { return userId; }
    public BankNumber bankNumber() { return bankNumber; }
    public CardDetails details() { return details; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
