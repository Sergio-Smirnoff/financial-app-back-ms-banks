package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.UserId;

public record DeleteCardCommand(String cardNumber, UserId userId) {}
