package com.financialapp.banks.infrastructure.messaging.payload;

import com.financialapp.banks.domain.common.model.Money;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PaymentEvent(
        Long userId,
        Long accountId,
        Money amount,
        String description,
        LocalDate date
) {}
