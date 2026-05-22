package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;
import java.util.List;

public record ImportCardExpensesCommand(
    String cardNumber,
    UserId userId,
    String arsAccountCbu,
    String usdAccountCbu,
    List<ImportedExpense> expenses
) {
    public record ImportedExpense(String description, Money amount, LocalDate date) {}
}
