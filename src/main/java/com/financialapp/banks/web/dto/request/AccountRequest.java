package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank @Size(max = 100) String bankName,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "CHECKING|SAVINGS|INVESTMENT") String type,
        @NotNull @DecimalMin("0.0") BigDecimal balance,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Boolean isActive,
        @NotBlank String cbu,
        String alias
) {}
