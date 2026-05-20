package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.CreateCardCommand;
import com.financialapp.banks.domain.model.card.Card;

public interface CreateCardUseCase {
    Card execute(CreateCardCommand command);
}
