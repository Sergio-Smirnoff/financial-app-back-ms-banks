package com.financialapp.banks.domain.usecase.fee.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.commons.core.domain.model.IvaTreatment;

import java.math.BigDecimal;

public record UpsertCardFeeScheduleCommand(
        UserId userId,
        String cardNumber,
        BigDecimal annualFee,
        BigDecimal internationalSurchargePct,
        String currency,
        IvaTreatment ivaTreatment
) {}
