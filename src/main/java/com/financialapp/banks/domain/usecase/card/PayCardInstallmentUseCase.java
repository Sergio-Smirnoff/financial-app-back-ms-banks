package com.financialapp.banks.domain.usecase.card;

import com.financialapp.banks.domain.usecase.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.domain.model.card.CardInstallment;

public interface PayCardInstallmentUseCase {
    CardInstallment execute(PayCardInstallmentCommand command);
}
