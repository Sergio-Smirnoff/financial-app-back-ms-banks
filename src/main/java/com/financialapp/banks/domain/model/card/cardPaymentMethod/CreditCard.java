package com.financialapp.banks.domain.model.card.cardPaymentMethod;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardNumber;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A credit card. Unlike a {@link DebitCard} it owns installment-based expenses.
 * The installments are set at construction and exposed read-only; any change
 * produces a new instance (the operations live in the use-case layer).
 */
public class CreditCard extends Card {

    private final List<CardInstallment> installments;

    public CreditCard(CardNumber cardNumber, UserId userId, BankNumber bankNumber,
                      CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(cardNumber, userId, bankNumber, details, createdAt, updatedAt, List.of());
    }

    public CreditCard(CardNumber cardNumber, UserId userId, BankNumber bankNumber,
                      CardDetails details, LocalDateTime createdAt, LocalDateTime updatedAt,
                      List<CardInstallment> installments) {
        super(cardNumber, userId, bankNumber, details, createdAt, updatedAt);
        this.installments = List.copyOf(installments);
    }

    public List<CardInstallment> installments() {
        return installments;
    }
}
