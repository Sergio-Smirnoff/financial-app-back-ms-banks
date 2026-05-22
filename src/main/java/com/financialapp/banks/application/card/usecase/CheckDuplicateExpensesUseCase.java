package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;

import java.util.List;

public interface CheckDuplicateExpensesUseCase {
    List<Integer> execute(String cardNumber, List<CreateCardExpenseCommand> expenses);
}
