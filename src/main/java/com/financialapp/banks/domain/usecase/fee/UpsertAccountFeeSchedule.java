package com.financialapp.banks.domain.usecase.fee;

import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.command.UpsertAccountFeeScheduleCommand;

public interface UpsertAccountFeeSchedule {
    AccountFeeSchedule execute(UpsertAccountFeeScheduleCommand command);
}
