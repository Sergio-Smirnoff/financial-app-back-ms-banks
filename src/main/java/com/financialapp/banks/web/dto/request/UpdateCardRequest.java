package com.financialapp.banks.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public record UpdateCardRequest(
        LocalDate expiringDate,
        @Min(1) @Max(31) Integer closingDay,
        @Min(1) @Max(31) Integer dueDay
) {}
