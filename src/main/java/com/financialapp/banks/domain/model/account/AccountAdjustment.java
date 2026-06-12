package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.DomainEvent;

import java.util.List;

public record AccountAdjustment(Account account, List<DomainEvent> events) {
    public AccountAdjustment {
        events = List.copyOf(events);
    }
}
