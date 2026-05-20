package com.financialapp.banks.application.card.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardId;

public record DeleteCardCommand(CardId id, UserId userId) {}
