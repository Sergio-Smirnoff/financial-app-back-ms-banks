package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;

public record LoanCreatedEvent(
        UserId userId,
        String destinationAccountCbu,
        Money amount,
        String loanName,
        LocalDate date
) implements DomainEvent {}
