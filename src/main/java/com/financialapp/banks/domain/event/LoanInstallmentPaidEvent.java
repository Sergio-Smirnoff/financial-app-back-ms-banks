package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;

import java.time.LocalDate;

public record LoanInstallmentPaidEvent(
        UserId userId,
        AccountId accountId,
        Money amount,
        String loanName,
        int installmentNumber,
        LocalDate paidDate
) implements DomainEvent {}
