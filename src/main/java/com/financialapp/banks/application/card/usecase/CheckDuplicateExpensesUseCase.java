package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.domain.common.model.UserId;

import java.util.List;

public interface CheckDuplicateExpensesUseCase {
    List<Integer> execute(String cardNumber, UserId userId, List<CreateCardExpenseCommand> expenses);
}
