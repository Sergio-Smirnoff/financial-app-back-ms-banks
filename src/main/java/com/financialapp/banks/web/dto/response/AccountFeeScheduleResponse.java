package com.financialapp.banks.web.dto.response;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import lombok.Builder;

@Builder
public record AccountFeeScheduleResponse(
        String cbu,
        String maintenanceFee,
        String transferFee,
        String currency,
        IvaTreatment ivaTreatment
) {}
