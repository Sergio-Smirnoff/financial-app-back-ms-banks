package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardId;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardExpenseCommand(
    CardId cardId,
    UserId userId,
    String description,
    BigDecimal totalAmount,
    String currency,
    int totalInstallments,
    LocalDate firstDueDate
) {}
