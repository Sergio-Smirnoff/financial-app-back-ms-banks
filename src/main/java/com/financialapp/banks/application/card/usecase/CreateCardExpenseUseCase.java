package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.domain.model.card.CardInstallment;

import java.util.List;

public interface CreateCardExpenseUseCase {
    List<CardInstallment> execute(CreateCardExpenseCommand command);
}
