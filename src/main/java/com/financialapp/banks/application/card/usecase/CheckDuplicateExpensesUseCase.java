package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.domain.model.card.CardId;

import java.util.List;

public interface CheckDuplicateExpensesUseCase {
    List<Integer> execute(CardId cardId, List<CreateCardExpenseCommand> expenses);
}
