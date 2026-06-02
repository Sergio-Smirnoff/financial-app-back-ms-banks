package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.loan.command.PayLoanInstallmentCommand;
import com.financialapp.banks.domain.usecase.loan.PayLoanInstallmentUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentPayment;
import com.financialapp.banks.domain.gateway.DomainEventPublisher;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.common.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PayLoanInstallmentUseCaseImpl implements PayLoanInstallmentUseCase {

    private final LoanRepository loanRepository;
    private final AdjustBalanceUseCase adjustBalance;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public LoanInstallment execute(PayLoanInstallmentCommand cmd) {
        Loan loan = loanRepository.findByIdAndUserId(cmd.loanId(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", cmd.loanId().value().toString()));

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        // The aggregate enforces active + paid-state + ownership and records the paid event.
        LoanInstallmentPayment payment = loan.payInstallment(cmd.installmentId(), paidDate, cmd.accountCbu());
        loanRepository.save(payment.loan());
        LoanInstallment paid = payment.installment();

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.accountCbu(), new Money(paid.amount().amount().negate(), paid.amount().currency())));

        eventPublisher.publishAll(payment.events());

        return paid;
    }
}
