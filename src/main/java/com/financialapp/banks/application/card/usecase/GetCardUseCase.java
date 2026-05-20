package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardId;

public interface GetCardUseCase {
    Card execute(CardId id, UserId userId);
}
