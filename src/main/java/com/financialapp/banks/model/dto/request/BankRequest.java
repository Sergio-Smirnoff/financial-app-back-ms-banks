package com.financialapp.banks.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BankRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String logoUrl
) {}
