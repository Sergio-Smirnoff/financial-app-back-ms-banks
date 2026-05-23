package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.UpdateCardCommand;
import com.financialapp.banks.domain.model.card.Card;

public interface UpdateCardUseCase {
    Card execute(UpdateCardCommand command);
}
