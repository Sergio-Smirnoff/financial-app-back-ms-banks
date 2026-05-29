package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.*;

public record UpdateAccountRequest(
        @Size(max = 100) String name,
        @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "balance must be a non-negative decimal string with up to 2 decimals") String balance,
        @Pattern(regexp = "[A-Za-z]{3}", message = "currency must be a 3-letter ISO 4217 code") String currency,
        Boolean isActive
) {}
