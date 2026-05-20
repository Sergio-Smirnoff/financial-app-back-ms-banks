package com.financialapp.banks.domain.event;

import com.financialapp.banks.domain.common.DomainEvent;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;

public record LoanInstallmentPaidEvent(
        UserId userId,
        String accountCbu,
        Money amount,
        String loanName,
        int installmentNumber,
        LocalDate paidDate
) implements DomainEvent {}
