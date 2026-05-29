package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;

public record RegisterCardExpenseCommand(
    String cardNumber,
    UserId userId,
    String description,
    Money amount,
    int totalInstallments,
    LocalDate firstDueDate
) {}
