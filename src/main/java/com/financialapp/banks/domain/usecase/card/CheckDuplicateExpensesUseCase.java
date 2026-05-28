package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.domain.common.model.UserId;

import java.util.List;

public interface CheckDuplicateExpensesUseCase {
    List<Integer> execute(String cardNumber, UserId userId, List<CreateCardExpenseCommand> expenses);
}
