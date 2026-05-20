package com.financialapp.banks.domain.model.card;

import java.time.LocalDate;

public record CardDetails(
    CardBrand brand,
    CardType cardType,
    LocalDate expiringDate,
    CardBilling billing
) {}
