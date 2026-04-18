package com.financialapp.banks.model.dto.request;

import com.financialapp.banks.model.enums.AccountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountRequest(
        @NotNull Long bankId,
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType type,
        @NotNull @DecimalMin("0.0") BigDecimal balance,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Boolean isActive
) {}
