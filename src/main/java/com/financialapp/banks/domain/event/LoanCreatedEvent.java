package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;

import java.time.LocalDate;

public record LoanCreatedEvent(
        UserId userId,
        AccountId destinationAccountId,
        Money amount,
        String loanName,
        LocalDate date
) implements DomainEvent {}
