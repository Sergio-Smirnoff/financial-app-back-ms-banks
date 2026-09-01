package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.common.model.Money;

import java.time.YearMonth;

public record CardDetails(
    CardBrand brand,
    CardType cardType,
    CardBehavior behavior,
    YearMonth expiringDate,
    CardBilling billing,
    Money creditLimit
) {}

