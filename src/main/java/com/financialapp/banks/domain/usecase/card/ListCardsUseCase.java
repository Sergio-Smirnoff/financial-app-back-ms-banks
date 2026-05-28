package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;

import java.util.List;

public interface ListCardsUseCase {
    List<Card> execute(UserId userId, BankName bankId);
}
