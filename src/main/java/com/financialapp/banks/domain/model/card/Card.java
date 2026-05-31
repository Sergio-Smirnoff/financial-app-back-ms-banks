package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.card.CardInstallmentNotSupportedException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Card {

    protected final CardNumber cardNumber;
    protected final UserId userId;
    protected final BankNumber bankNumber;
    protected final CardDetails details;
    protected final LocalDateTime createdAt;
    protected final LocalDateTime updatedAt;
    protected final List<CardInstallment> installments = new ArrayList<>();

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

    /**
     * Guards that this card supports installment-based expenses.
     * @throws CardInstallmentNotSupportedException when the card behaves as instant payment (e.g. debit).
     */
    public void ensureSupportsInstallments() {
        if (details.behavior() == CardBehavior.INSTANT_PAYMENT) {
            throw new CardInstallmentNotSupportedException(cardNumber.value());
        }
    }

    public List<CardInstallment> installments() {
        return List.copyOf(installments);
    }

    /** Hydration hook for persistence — replaces the installment collection in one shot. */
    public void restoreInstallments(List<CardInstallment> loaded) {
        installments.clear();
        installments.addAll(loaded);
    }

    /**
     * Registers a new installment-based expense on this card. Splits {@code total} across
     * {@code totalInstallments} via {@link CardInstallment#schedule} and appends them.
     * @throws CardInstallmentNotSupportedException when the card cannot carry installments.
     */
    public List<CardInstallment> registerExpense(String description, Money total,
                                                 int totalInstallments, LocalDate firstDueDate) {
        ensureSupportsInstallments();
        List<CardInstallment> created = CardInstallment.schedule(
                cardNumber.value(), description, total, totalInstallments, firstDueDate);
        installments.addAll(created);
        return created;
    }

    /**
     * Pays the installment with the given id on {@code paidDate}, replacing it in place.
     * @throws ResourceNotFoundException if the id is not on this card.
     * @throws com.financialapp.banks.domain.exception.card.CardInstallmentAlreadyPaidException if already paid.
     */
    public CardInstallmentPayment payInstallment(CardInstallmentId installmentId, LocalDate paidDate,
                                                 String paidFromAccountCbu) {
        for (int index = 0; index < installments.size(); index++) {
            CardInstallment current = installments.get(index);
            if (current.id().equals(installmentId)) {
                CardInstallment paid = current.pay(paidDate);
                installments.set(index, paid);
                DomainEvent event = new CardInstallmentPaidEvent(
                        userId, paidFromAccountCbu,
                        new Money(paid.amount().amount().negate(), paid.amount().currency()),
                        paid.description(), paid.installmentNumber(), paid.totalInstallments(), paidDate);
                return new CardInstallmentPayment(paid, List.of(event));
            }
        }
        throw new ResourceNotFoundException("CardInstallment",
                installmentId.value() == null ? "new" : installmentId.value().toString());
    }

    /** True when the card has at least one unpaid installment (blocks cancellation). */
    public boolean hasUnpaidInstallments() {
        return installments.stream().anyMatch(installment -> !installment.paid());
    }

    /** True when an installment with the same description, amount and due date already exists. */
    public boolean hasInstallmentMatching(String description, Money amount, LocalDate dueDate) {
        return installments.stream().anyMatch(installment ->
                installment.description().equals(description)
                        && installment.amount().amount().compareTo(amount.amount()) == 0
                        && installment.dueDate().equals(dueDate));
    }

    public CardNumber cardNumber() { return cardNumber; }
    public UserId userId() { return userId; }
    public BankNumber bankNumber() { return bankNumber; }
    public CardDetails details() { return details; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
