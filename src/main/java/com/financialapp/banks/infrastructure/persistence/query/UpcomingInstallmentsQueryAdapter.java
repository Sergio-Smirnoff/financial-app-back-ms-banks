package com.financialapp.banks.infrastructure.persistence.query;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.query.UpcomingInstallment;
import com.financialapp.banks.domain.query.UpcomingInstallmentsQuery;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanInstallmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpcomingInstallmentsQueryAdapter implements UpcomingInstallmentsQuery {

    private final LoanInstallmentJpaRepository loanInstallments;
    private final CardInstallmentJpaRepository cardInstallments;

    @Override
    public List<UpcomingInstallment> findUnpaidBetween(UserId userId, LocalDate from, LocalDate to) {
        List<UpcomingInstallment> rows = new ArrayList<>();

        loanInstallments.findUpcomingUnpaidByUser(userId.value(), from, to).forEach(li ->
                rows.add(new UpcomingInstallment(
                        li.getId(), "LOAN", li.getLoan().getName(),
                        new Money(li.getAmount(), Currency.getInstance(li.getLoan().getCurrency())),
                        li.getDueDate(), li.getInstallmentNumber(),
                        li.getLoan().getTotalInstallments(), li.isPaid())));

        cardInstallments.findUpcomingUnpaid(userId.value(), from, to).forEach(ci ->
                rows.add(new UpcomingInstallment(
                        ci.getId(), "CARD", ci.getDescription(),
                        new Money(ci.getAmount(), Currency.getInstance(ci.getCurrency())),
                        ci.getDueDate(), ci.getInstallmentNumber(),
                        ci.getTotalInstallments(), ci.isPaid())));

        return rows;
    }
}
