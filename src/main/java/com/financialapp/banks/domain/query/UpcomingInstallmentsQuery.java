package com.financialapp.banks.domain.query;

import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;
import java.util.List;

/** Read-only projection of a user's unpaid loan + card installments due within a range. */
public interface UpcomingInstallmentsQuery {
    List<UpcomingInstallment> findUnpaidBetween(UserId userId, LocalDate from, LocalDate to);
}
