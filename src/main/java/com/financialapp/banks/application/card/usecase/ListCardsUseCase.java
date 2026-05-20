package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankId;
import com.financialapp.banks.domain.model.card.Card;

import java.util.List;

public interface ListCardsUseCase {
    List<Card> execute(UserId userId, BankId bankId);
}
