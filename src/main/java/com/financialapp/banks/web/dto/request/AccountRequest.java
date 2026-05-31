package com.financialapp.banks.web.dto.request;

import com.financialapp.banks.domain.model.account.AccountType;
import jakarta.validation.constraints.*;

public record AccountRequest(
        @NotBlank @Pattern(regexp = "\\d{3}", message = "bankNumber must be exactly 3 digits") String bankNumber,
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType type,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}", message = "currency must be a 3-letter ISO 4217 code") String currency,
        Boolean isActive,
        @NotBlank @Pattern(regexp = "\\d{22}", message = "cbu must be exactly 22 digits") String cbu,
        String alias
) {}
