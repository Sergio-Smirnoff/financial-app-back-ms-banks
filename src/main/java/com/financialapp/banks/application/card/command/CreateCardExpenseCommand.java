package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardId;
import com.financialapp.banks.domain.common.model.Money;

import java.time.LocalDate;

public record CreateCardExpenseCommand(
    CardId cardId,
    UserId userId,
    String description,
    Money amount,
    int totalInstallments,
    LocalDate firstDueDate
) {}
