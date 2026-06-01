package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.Card;

public interface GetCardUseCase {
    Card execute(String cardNumber, UserId userId);
}
