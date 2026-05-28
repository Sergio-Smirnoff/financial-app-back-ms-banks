package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.UpdateCardCommand;
import com.financialapp.banks.domain.model.card.Card;

public interface UpdateCardUseCase {
    Card execute(UpdateCardCommand command);
}
