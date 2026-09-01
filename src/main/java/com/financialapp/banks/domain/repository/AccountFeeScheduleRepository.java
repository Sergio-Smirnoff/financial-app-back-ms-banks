package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;

import java.util.List;
import java.util.Optional;

public interface AccountFeeScheduleRepository {
    AccountFeeSchedule save(AccountFeeSchedule schedule);
    Optional<AccountFeeSchedule> findByAccountCbu(Cbu cbu);
    List<AccountFeeSchedule> findByOwner(UserId userId);
}
