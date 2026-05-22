package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.time.LocalDateTime;

public abstract class Card {

    protected final String cardNumber;
    protected final UserId userId;
    protected final BankName bankName;
    protected final CardDetails details;
    protected final LocalDateTime createdAt;
    protected final LocalDateTime updatedAt;

    protected Card(String cardNumber, UserId userId, BankName bankName,
                   CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.cardNumber = cardNumber;
        this.userId = userId;
        this.bankName = bankName;
        this.details = details;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String cardNumber() { return cardNumber; }
    public UserId userId() { return userId; }
    public BankName bankName() { return bankName; }
    public CardDetails details() { return details; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
