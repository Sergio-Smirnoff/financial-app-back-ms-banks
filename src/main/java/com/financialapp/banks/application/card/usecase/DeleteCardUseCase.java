package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.DeleteCardCommand;

public interface DeleteCardUseCase {
    void execute(DeleteCardCommand command);
}
