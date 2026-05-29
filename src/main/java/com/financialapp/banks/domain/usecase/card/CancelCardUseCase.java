package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.CancelCardCommand;

public interface CancelCardUseCase {
    void execute(CancelCardCommand command);
}
