package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;

public record CardInstallmentPaidEvent(
        UserId userId,
        String accountCbu,
        Money amount,
        String description,
        int installmentNumber,
        int totalInstallments,
        LocalDate paidDate
) implements DomainEvent {}
