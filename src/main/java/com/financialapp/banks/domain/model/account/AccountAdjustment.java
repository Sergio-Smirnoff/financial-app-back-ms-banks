package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.DomainEvent;

import java.util.List;

/**
 * Result of an {@link Account} balance change: the resulting (immutable) account plus the
 * domain events the aggregate recorded for the operation. The application drains and publishes
 * the events; it does not author them.
 */
public record AccountAdjustment(Account account, List<DomainEvent> events) {
    public AccountAdjustment {
        events = List.copyOf(events);
    }
}
