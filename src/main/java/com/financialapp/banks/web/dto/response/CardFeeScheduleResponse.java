package com.financialapp.banks.web.dto.response;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import lombok.Builder;

@Builder
public record CardFeeScheduleResponse(
        String cardNumber,
        String annualFee,
        String internationalSurchargePct,
        String currency,
        IvaTreatment ivaTreatment
) {}
