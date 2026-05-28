package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.ImportCardExpensesCommand;

public interface ImportCardExpensesUseCase {
    BatchImportResult execute(ImportCardExpensesCommand command);
}
