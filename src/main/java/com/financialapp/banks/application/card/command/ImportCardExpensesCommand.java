package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.card.CardId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ImportCardExpensesCommand(
    CardId cardId,
    UserId userId,
    AccountId arsAccountId,
    AccountId usdAccountId,
    boolean bypassBalance,
    List<ImportedExpense> expenses
) {
    public record ImportedExpense(String description, BigDecimal amount, String currency, LocalDate date) {}
}
