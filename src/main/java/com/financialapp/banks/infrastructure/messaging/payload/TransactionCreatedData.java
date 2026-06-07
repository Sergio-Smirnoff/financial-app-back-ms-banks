package com.financialapp.banks.infrastructure.messaging.payload;

import java.math.BigDecimal;

public record TransactionCreatedData(
        Long transactionId,
        String accountCbu,
        BigDecimal amount,
        String currency
) {}
