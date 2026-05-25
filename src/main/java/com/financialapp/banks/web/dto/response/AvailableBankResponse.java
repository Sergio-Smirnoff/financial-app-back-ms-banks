package com.financialapp.banks.web.dto.response;

public record AvailableBankResponse(
        String name,
        String displayName,
        String logoUrl
) {}
