package com.financialapp.banks.web.dto.request;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CardFeeScheduleRequest(
        BigDecimal annualFee,
        BigDecimal internationalSurchargePct,
        String currency,
        @NotNull IvaTreatment ivaTreatment
) {}
