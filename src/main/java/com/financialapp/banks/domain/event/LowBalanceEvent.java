package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;

public record LowBalanceEvent(
        UserId userId,
        AccountId accountId,
        BankName bankName,
        String accountName,
        Money balance
) implements DomainEvent {}
