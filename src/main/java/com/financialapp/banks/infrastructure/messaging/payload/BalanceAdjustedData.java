package com.financialapp.banks.infrastructure.messaging.payload;

import java.math.BigDecimal;

public record BalanceAdjustedData(
        Long userId,
        String accountName,
        String accountCbu,
        String bankNumber,
        BigDecimal amount,
        String currency,
        boolean credit
) {}
