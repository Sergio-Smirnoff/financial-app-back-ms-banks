package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardInstallmentId;

import java.time.LocalDate;

public record PayCardInstallmentCommand(
    String cardNumber,
    CardInstallmentId installmentId,
    UserId userId,
    String accountCbu,
    LocalDate paidDate
) {}
