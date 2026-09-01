package com.financialapp.banks.domain.usecase.fee;

import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.command.UpsertCardFeeScheduleCommand;

public interface UpsertCardFeeSchedule {
    CardFeeSchedule execute(UpsertCardFeeScheduleCommand command);
}
