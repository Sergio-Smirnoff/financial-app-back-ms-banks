package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.UserId;

import java.math.BigDecimal;
import java.time.YearMonth;

public record UpdateCardCommand(
        String cardNumber,
        UserId userId,
        YearMonth expiringDate,
        Integer closingDay,
        Integer dueDay,
        BigDecimal creditLimit
) {}
