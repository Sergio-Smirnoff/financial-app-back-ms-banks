package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateAccountRequest(
        @Size(max = 100) String name,
        @DecimalMin("0.0") BigDecimal balance,
        @Pattern(regexp = "[A-Za-z]{3}", message = "currency must be a 3-letter ISO 4217 code") String currency,
        Boolean isActive
) {}
