package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;

import java.time.YearMonth;

public record UpdateCardCommand(
        String cardNumber,
        UserId userId,
        YearMonth expiringDate,
        Integer closingDay,
        Integer dueDay
) {}
