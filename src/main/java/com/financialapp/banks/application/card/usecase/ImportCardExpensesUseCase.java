package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.ImportCardExpensesCommand;

public interface ImportCardExpensesUseCase {
    BatchImportResult execute(ImportCardExpensesCommand command);
}
