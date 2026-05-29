package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.DomainEvent;

import java.util.List;

/**
 * Result of paying a loan installment: the updated {@link Loan}, the paid {@link LoanInstallment},
 * and the domain events the aggregate recorded. The application drains and publishes the events.
 */
public record LoanInstallmentPayment(Loan loan, LoanInstallment installment, List<DomainEvent> events) {
    public LoanInstallmentPayment {
        events = List.copyOf(events);
    }
}
