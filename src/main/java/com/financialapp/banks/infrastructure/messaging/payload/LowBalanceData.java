package com.financialapp.banks.infrastructure.messaging.payload;

import java.math.BigDecimal;

public record LowBalanceData(
        Long userId,
        String accountName,
        String accountCbu,
        String bankNumber,
        BigDecimal balance,
        String currency
) {}
