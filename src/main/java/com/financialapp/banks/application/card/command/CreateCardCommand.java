package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;

import java.time.LocalDate;

public record CreateCardCommand(
    UserId userId,
    BankName bankName,
    CardBrand brand,
    CardType cardType,
    CardBehavior behavior,
    String number,
    LocalDate expiringDate,
    int closingDay,
    int dueDay
) {}
