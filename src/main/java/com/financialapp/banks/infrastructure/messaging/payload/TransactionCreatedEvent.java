package com.financialapp.banks.infrastructure.messaging.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreatedEvent(
        Long transactionId,
        Long userId,
        String accountCbu,
        BigDecimal amount,
        String currency,
        LocalDate timestamp
) {}
