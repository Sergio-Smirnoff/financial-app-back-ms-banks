package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;

import java.time.LocalDateTime;

public abstract class Card {

    protected final CardNumber cardNumber;
    protected final UserId userId;
    protected final BankName bankName;
    protected final CardDetails details;
    protected final LocalDateTime createdAt;
    protected final LocalDateTime updatedAt;

    protected Card(CardNumber cardNumber, UserId userId, BankName bankName,
                   CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cardNumber = cardNumber;
        this.userId = userId;
        this.bankName = bankName;
        this.details = details;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Card create(String number, UserId userId, BankName bankName,
                              CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        CardNumber cardNumber = new CardNumber(number);
        return details.behavior() == CardBehavior.INSTANT_PAYMENT
                ? new DebitCard(cardNumber, userId, bankName, details, createdAt, updatedAt)
                : new CreditCard(cardNumber, userId, bankName, details, createdAt, updatedAt);
    }

    public CardNumber cardNumber() { return cardNumber; }
    public UserId userId() { return userId; }
    public BankName bankName() { return bankName; }
    public CardDetails details() { return details; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
