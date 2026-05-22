package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;

public record DeleteCardCommand(String cardNumber, UserId userId) {}
