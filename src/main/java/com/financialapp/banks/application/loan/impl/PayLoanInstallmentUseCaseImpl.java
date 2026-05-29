package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.domain.usecase.loan.command.PayLoanInstallmentCommand;
import com.financialapp.banks.domain.usecase.loan.PayLoanInstallmentUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.event.LoanInstallmentPaidEvent;
import com.financialapp.banks.domain.common.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PayLoanInstallmentUseCaseImpl implements PayLoanInstallmentUseCase {

    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final AdjustBalanceUseCaseImpl adjustBalance;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public LoanInstallment execute(PayLoanInstallmentCommand cmd) {
        Loan loan = loanRepository.findByIdAndUserId(cmd.loanId(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", cmd.loanId().value().toString()));
        loan.ensureActive();

        LoanInstallment installment = installmentRepository.findById(cmd.installmentId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanInstallment", cmd.installmentId().value().toString()));
        installment.ensureBelongsTo(cmd.loanId());

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        LoanInstallment paid = installment.pay(paidDate);

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.accountCbu(), new Money(installment.amount().amount().negate(), installment.amount().currency())));

        LoanInstallment saved = installmentRepository.save(paid);

        loanRepository.save(loan.registerInstallmentPaid());

        eventPublisher.publish(new LoanInstallmentPaidEvent(
                cmd.userId(),
                cmd.accountCbu(),
                new Money(saved.amount().amount().negate(), saved.amount().currency()),
                loan.name(),
                saved.installmentNumber(),
                paidDate
        ));

        return saved;
    }
}
