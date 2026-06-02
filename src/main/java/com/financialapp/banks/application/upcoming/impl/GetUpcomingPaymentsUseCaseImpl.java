package com.financialapp.banks.application.upcoming.impl;

import com.financialapp.banks.domain.usecase.upcoming.GetUpcomingPaymentsUseCase;
import com.financialapp.banks.domain.usecase.upcoming.UpcomingPayment;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GetUpcomingPaymentsUseCaseImpl implements GetUpcomingPaymentsUseCase {

    private final LoanRepository loanRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingPayment> execute(UserId userId, LocalDate from, LocalDate to) {
        Stream<UpcomingPayment> loanPayments = loanRepository
                .findWithUpcomingUnpaidInstallments(userId, from, to).stream()
                .flatMap(loan -> loan.installments().stream()
                        .filter(installment -> isUnpaidWithin(installment, from, to))
                        .map(installment -> toLoanPayment(loan, installment)));

        Stream<UpcomingPayment> cardPayments = cardRepository
                .findUpcomingUnpaidInstallments(userId, from, to).stream()
                .map(this::toCardPayment);

        return Stream.concat(loanPayments, cardPayments)
                .sorted(Comparator.comparing(UpcomingPayment::dueDate))
                .toList();
    }

    private boolean isUnpaidWithin(LoanInstallment installment, LocalDate from, LocalDate to) {
        return !installment.paid()
                && !installment.dueDate().isBefore(from)
                && !installment.dueDate().isAfter(to);
    }

    private UpcomingPayment toLoanPayment(Loan loan, LoanInstallment installment) {
        return new UpcomingPayment(
                installment.id().value(), "LOAN", loan.name(),
                installment.amount(), installment.dueDate(),
                installment.installmentNumber(), loan.totalInstallments(), installment.paid());
    }

    private UpcomingPayment toCardPayment(CardInstallment installment) {
        return new UpcomingPayment(
                installment.id().value(), "CARD", installment.description(),
                installment.amount(), installment.dueDate(),
                installment.installmentNumber(), installment.totalInstallments(), installment.paid());
    }
}
