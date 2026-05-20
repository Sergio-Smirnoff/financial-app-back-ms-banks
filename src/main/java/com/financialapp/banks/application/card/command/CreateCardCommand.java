package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankId;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;

import java.time.LocalDate;

public record CreateCardCommand(
    UserId userId,
    BankId bankId,
    CardBrand brand,
    CardType cardType,
    CardBehavior behavior,
    String last4Digits,
    LocalDate expiringDate,
    int closingDay,
    int dueDay
) {}
