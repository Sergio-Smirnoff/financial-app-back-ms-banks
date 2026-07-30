package com.financialapp.banks.web.dto.request;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountFeeScheduleRequest(
        BigDecimal maintenanceFee,
        BigDecimal transferFee,
        String currency,
        @NotNull IvaTreatment ivaTreatment
) {}
