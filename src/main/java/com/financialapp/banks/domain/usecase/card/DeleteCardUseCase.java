package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.DeleteCardCommand;

public interface DeleteCardUseCase {
    void execute(DeleteCardCommand command);
}
