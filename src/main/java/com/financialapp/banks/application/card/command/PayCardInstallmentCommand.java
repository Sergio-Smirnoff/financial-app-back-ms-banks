package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.card.CardId;
import com.financialapp.banks.domain.model.card.CardInstallmentId;

import java.time.LocalDate;

public record PayCardInstallmentCommand(
    CardId cardId,
    CardInstallmentId installmentId,
    UserId userId,
    AccountId accountId,
    LocalDate paidDate,
    boolean bypassBalance
) {}
