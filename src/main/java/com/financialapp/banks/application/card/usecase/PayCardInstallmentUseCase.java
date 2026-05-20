package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.application.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.domain.model.card.CardInstallment;

public interface PayCardInstallmentUseCase {
    CardInstallment execute(PayCardInstallmentCommand command);
}
