package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;

public record UpdateCardCommand(
        String cardNumber,
        UserId userId,
        LocalDate expiringDate,
        Integer closingDay,
        Integer dueDay
) {}
