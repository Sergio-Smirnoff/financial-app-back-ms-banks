package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.IssueCardCommand;
import com.financialapp.banks.domain.model.card.Card;

public interface IssueCardUseCase {
    Card execute(IssueCardCommand command);
}
