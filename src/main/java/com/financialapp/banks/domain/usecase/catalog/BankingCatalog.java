package com.financialapp.banks.domain.usecase.catalog;

import java.util.List;

public record BankingCatalog(
        List<String> accountTypes,
        List<String> cardTypes,
        List<String> cardBrands,
        List<String> cardBehaviors
) {}
