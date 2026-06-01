package com.financialapp.banks.application.upcoming.impl;

import com.financialapp.banks.domain.usecase.upcoming.GetUpcomingPaymentsUseCase;
import com.financialapp.banks.domain.usecase.upcoming.UpcomingPayment;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.query.UpcomingInstallment;
import com.financialapp.banks.domain.query.UpcomingInstallmentsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUpcomingPaymentsUseCaseImpl implements GetUpcomingPaymentsUseCase {

    private final UpcomingInstallmentsQuery upcomingInstallmentsQuery;

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingPayment> execute(UserId userId, LocalDate from, LocalDate to) {
        return upcomingInstallmentsQuery.findUnpaidBetween(userId, from, to).stream()
                .map(this::toPayment)
                .sorted(Comparator.comparing(UpcomingPayment::dueDate))
                .toList();
    }

    private UpcomingPayment toPayment(UpcomingInstallment row) {
        return new UpcomingPayment(
                row.installmentId(), row.type(), row.description(),
                row.amount(), row.dueDate(),
                row.installmentNumber(), row.totalInstallments(), row.paid());
    }
}
