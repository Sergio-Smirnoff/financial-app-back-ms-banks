package com.financialapp.banks.web.dto.response;

import java.util.List;

public record BankingCatalogResponse(
        List<String> accountTypes,
        List<String> cardTypes,
        List<String> cardBrands,
        List<String> cardBehaviors
) {}
