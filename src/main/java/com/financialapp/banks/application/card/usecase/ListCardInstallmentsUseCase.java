package com.financialapp.banks.application.card.usecase;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardInstallment;

import java.util.List;

public interface ListCardInstallmentsUseCase {
    List<CardInstallment> execute(String cardNumber, UserId userId);
}
