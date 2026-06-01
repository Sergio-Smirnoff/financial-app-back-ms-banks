package com.financialapp.banks.infrastructure.messaging.payload;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record PaymentEvent(
        Long userId,
        String accountCbu,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate date
) {}
