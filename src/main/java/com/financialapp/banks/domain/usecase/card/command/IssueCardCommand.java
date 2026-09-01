package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;

import java.math.BigDecimal;
import java.time.YearMonth;

public record IssueCardCommand(
    UserId userId,
    BankNumber bankNumber,
    CardBrand brand,
    CardType cardType,
    CardBehavior behavior,
    String number,
    YearMonth expiringDate,
    int closingDay,
    int dueDay,
    BigDecimal creditLimit
) {}
