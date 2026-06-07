package com.financialapp.banks.infrastructure.messaging.payload;

public record CardExpiringData(
        Long userId,
        String cardNumber,
        String bankNumber,
        String expiringDate
) {}
