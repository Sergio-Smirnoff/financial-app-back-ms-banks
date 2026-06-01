package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.UserId;

public record CancelCardCommand(String cardNumber, UserId userId) {}
