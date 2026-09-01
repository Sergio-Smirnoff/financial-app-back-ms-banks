package com.financialapp.banks.domain.model.fee;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.fee.InvalidFeeScheduleException;
import com.financialapp.commons.core.domain.model.IvaTreatment;

public record AccountFeeSchedule(
        AccountFeeScheduleId id,
        Cbu accountCbu,
        Money maintenanceFee,
        Money transferFee,
        IvaTreatment ivaTreatment
) {
    public AccountFeeSchedule {
        if (accountCbu == null) {
            throw new InvalidFeeScheduleException("accountCbu must not be null");
        }
        if (ivaTreatment == null) {
            throw new InvalidFeeScheduleException("ivaTreatment must not be null");
        }
    }
}
