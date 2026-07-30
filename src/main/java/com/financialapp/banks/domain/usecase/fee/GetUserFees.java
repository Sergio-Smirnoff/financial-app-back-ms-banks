package com.financialapp.banks.domain.usecase.fee;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.usecase.fee.response.UserFeesResult;

public interface GetUserFees {
    UserFeesResult execute(UserId userId);
}
