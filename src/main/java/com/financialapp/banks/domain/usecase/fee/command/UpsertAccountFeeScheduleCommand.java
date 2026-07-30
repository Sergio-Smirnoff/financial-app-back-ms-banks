package com.financialapp.banks.domain.usecase.fee.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.commons.core.domain.model.IvaTreatment;

import java.math.BigDecimal;

public record UpsertAccountFeeScheduleCommand(
        UserId userId,
        String cbu,
        BigDecimal maintenanceFee,
        BigDecimal transferFee,
        String currency,
        IvaTreatment ivaTreatment
) {}
