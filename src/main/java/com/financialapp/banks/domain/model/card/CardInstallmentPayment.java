package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.DomainEvent;

import java.util.List;

/**
 * Result of paying a card installment: the paid {@link CardInstallment} plus the domain events
 * the {@link Card} aggregate recorded. The application drains and publishes the events.
 */
public record CardInstallmentPayment(CardInstallment installment, List<DomainEvent> events) {
    public CardInstallmentPayment {
        events = List.copyOf(events);
    }
}
