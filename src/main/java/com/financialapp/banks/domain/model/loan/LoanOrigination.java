package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.DomainEvent;

import java.util.List;

/**
 * Result of originating a {@link Loan}: the new (unpersisted) loan with its schedule plus the
 * domain events the aggregate recorded. The application drains and publishes the events.
 */
public record LoanOrigination(Loan loan, List<DomainEvent> events) {
    public LoanOrigination {
        events = List.copyOf(events);
    }
}
