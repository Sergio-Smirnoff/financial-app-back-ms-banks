package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.CreateCardCommand;
import com.financialapp.banks.domain.model.card.Card;

public interface CreateCardUseCase {
    Card execute(CreateCardCommand command);
}
