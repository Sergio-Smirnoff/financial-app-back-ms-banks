package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.RegisterCardExpenseCommand;
import com.financialapp.banks.domain.model.card.CardInstallment;

import java.util.List;

public interface RegisterCardExpenseUseCase {
    List<CardInstallment> execute(RegisterCardExpenseCommand command);
}
