package com.financialapp.banks.web.dto.request;

import com.financialapp.banks.domain.model.account.AccountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank @Size(max = 100) String bankName,
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType type,
        @NotNull @DecimalMin("0.0") BigDecimal balance,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Boolean isActive,
        @NotBlank String cbu,
        String alias
) {}
