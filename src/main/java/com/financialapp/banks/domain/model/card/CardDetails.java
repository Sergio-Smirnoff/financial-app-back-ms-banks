package com.financialapp.banks.domain.model.card;

import java.time.LocalDate;

public record CardDetails(
    CardBrand brand,
    CardType cardType,
    CardBehavior behavior,
    String cardNumber,
    LocalDate expiringDate,
    CardBilling billing
) {}
