package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;

import java.time.LocalDate;

public record CardInstallmentPaidEvent(
        UserId userId,
        AccountId accountId,
        Money amount,
        String description,
        int installmentNumber,
        int totalInstallments,
        LocalDate paidDate
) implements DomainEvent {}
