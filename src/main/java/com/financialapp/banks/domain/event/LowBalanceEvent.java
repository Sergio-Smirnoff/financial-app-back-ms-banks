package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;

public record LowBalanceEvent(
        UserId userId,
        String accountCbu,
        BankNumber bankNumber,
        String accountName,
        Money balance
) implements DomainEvent {}
