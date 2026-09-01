package com.financialapp.banks.domain.model.fee;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.fee.InvalidFeeScheduleException;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.commons.core.domain.model.IvaTreatment;

import java.math.BigDecimal;

public record CardFeeSchedule(
        CardFeeScheduleId id,
        CardNumber cardNumber,
        Money annualFee,
        BigDecimal internationalSurchargePct,
        IvaTreatment ivaTreatment
) {
    public CardFeeSchedule {
        if (cardNumber == null) {
            throw new InvalidFeeScheduleException("cardNumber must not be null");
        }
        if (ivaTreatment == null) {
            throw new InvalidFeeScheduleException("ivaTreatment must not be null");
        }
        if (internationalSurchargePct != null) {
            if (internationalSurchargePct.compareTo(BigDecimal.ZERO) <= 0 || internationalSurchargePct.compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidFeeScheduleException("internationalSurchargePct must be strictly > 0 and <= 100, got " + internationalSurchargePct);
            }
        }
    }
}
