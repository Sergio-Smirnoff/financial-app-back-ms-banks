package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

public record BalanceAdjustedEvent(
        UserId userId,
        String accountCbu,
        BankName bankName,
        String accountName,
        Money delta
) implements DomainEvent {}
