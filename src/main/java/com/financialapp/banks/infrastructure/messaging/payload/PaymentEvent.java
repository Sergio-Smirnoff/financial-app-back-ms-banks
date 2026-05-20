package com.financialapp.banks.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentEvent(
        Long userId,
        Long accountId,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate date
) {}
