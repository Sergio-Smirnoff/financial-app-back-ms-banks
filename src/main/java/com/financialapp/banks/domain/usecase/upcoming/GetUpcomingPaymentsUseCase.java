package com.financialapp.banks.domain.usecase.upcoming;

import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;
import java.util.List;

public interface GetUpcomingPaymentsUseCase {
    List<UpcomingPayment> execute(UserId userId, LocalDate from, LocalDate to);
}
