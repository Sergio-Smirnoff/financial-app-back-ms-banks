package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.domain.model.card.CardInstallment;

import java.util.List;

public interface CreateCardExpenseUseCase {
    List<CardInstallment> execute(CreateCardExpenseCommand command);
}
