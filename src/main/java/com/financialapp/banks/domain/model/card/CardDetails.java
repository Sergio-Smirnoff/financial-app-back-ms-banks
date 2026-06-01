package com.financialapp.banks.domain.model.card;

import java.time.YearMonth;

public record CardDetails(
    CardBrand brand,
    CardType cardType,
    CardBehavior behavior,
    YearMonth expiringDate,
    CardBilling billing
) {}
