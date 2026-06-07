package com.financialapp.banks.infrastructure.messaging.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardInstallmentDueData(
        Long userId,
        String cardNumber,
        Long installmentId,
        int installmentNumber,
        int totalInstallments,
        String description,
        LocalDate dueDate,
        BigDecimal amount,
        String currency
) {}
