package com.financialapp.banks.application.upcoming.impl;

import com.financialapp.banks.domain.usecase.upcoming.GetUpcomingPaymentsUseCase;
import com.financialapp.banks.domain.usecase.upcoming.UpcomingPayment;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUpcomingPaymentsUseCaseImpl implements GetUpcomingPaymentsUseCase {

    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CardInstallmentRepository cardInstallmentRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingPayment> execute(UserId userId, LocalDate from, LocalDate to) {
        List<UpcomingPayment> results = new ArrayList<>();

        List<LoanInstallment> loanInsts = loanInstallmentRepository.findUpcomingUnpaid(from, to);
        for (LoanInstallment li : loanInsts) {
            loanRepository.findById(li.loanId()).ifPresent(loan -> {
                if (loan.userId().equals(userId)) {
                    results.add(new UpcomingPayment(
                            li.id().value(), "LOAN", loan.name(),
                            li.amount(), li.dueDate(),
                            li.installmentNumber(), loan.totalInstallments(), li.paid()
                    ));
                }
            });
        }

        List<CardInstallment> cardInsts = cardInstallmentRepository.findUpcomingUnpaid(userId, from, to);
        for (CardInstallment ci : cardInsts) {
            results.add(new UpcomingPayment(
                    ci.id().value(), "CARD", ci.description(),
                    ci.amount(), ci.dueDate(),
                    ci.installmentNumber(), ci.totalInstallments(), ci.paid()
            ));
        }

        results.sort(Comparator.comparing(UpcomingPayment::dueDate));
        return results;
    }
}
