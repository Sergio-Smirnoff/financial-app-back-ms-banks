package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.event.CardInstallmentPaidEvent;
import com.financialapp.banks.domain.model.card.CardInstallment;

import java.time.LocalDate;

/**
 * Builds card domain events. Lives in the domain so the application layer never
 * constructs {@code ..domain.event..} types directly (it only publishes them).
 */
public final class CardInstallmentEventFactory {

    private CardInstallmentEventFactory() {}

    /** Event for a just-paid installment; the delta is the refund (negative) amount. */
    public static DomainEvent installmentPaid(UserId userId, String accountCbu,
                                              CardInstallment paid, LocalDate paidDate) {
        Money refund = new Money(paid.amount().amount().negate(), paid.amount().currency());
        return new CardInstallmentPaidEvent(userId, accountCbu, refund,
                paid.description(), paid.installmentNumber(), paid.totalInstallments(), paidDate);
    }
}
