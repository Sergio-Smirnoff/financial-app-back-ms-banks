package com.financialapp.banks.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.YearMonth;

public record UpdateCardRequest(
        @JsonFormat(pattern = "MM/yy")
        @Schema(type = "string", example = "08/30", description = "Card expiry in MM/YY format")
        YearMonth expiringDate,
        @Min(1) @Max(31) Integer closingDay,
        @Min(1) @Max(31) Integer dueDay
) {}
