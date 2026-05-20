package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.application.loan.command.PayLoanInstallmentCommand;
import com.financialapp.banks.application.loan.usecase.PayLoanInstallmentUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.infrastructure.messaging.payload.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + cmd.loanId().value()));

        if (!loan.active()) {
            throw new BusinessException("Loan is already closed");
        }

        LoanInstallment installment = installmentRepository.findById(cmd.installmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found: " + cmd.installmentId().value()));

        if (!installment.loanId().equals(cmd.loanId())) {
            throw new BusinessException("Installment does not belong to the specified loan");
        }
        if (installment.paid()) {
            throw new BusinessException("Installment is already paid");
        }

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.accountId(), installment.amount().negate(), loan.details().currency()));

        LocalDate paidDate = cmd.paidDate() != null ? cmd.paidDate() : LocalDate.now();
        LoanInstallment paid = new LoanInstallment(
                installment.id(),
                installment.loanId(),
                installment.installmentNumber(),
                installment.amount(),
                installment.dueDate(),
                true,
                paidDate,
                installment.createdAt(),
                LocalDateTime.now()
        );
        LoanInstallment saved = installmentRepository.save(paid);

        int remaining = loan.remainingInstallments() - 1;
        Loan updated = new Loan(
                loan.id(), loan.userId(), loan.bankId(), loan.name(), loan.details(),
                remaining, loan.startDate(), remaining > 0, loan.createdAt(), LocalDateTime.now()
        );
        loanRepository.save(updated);

        eventPublisher.publish(PaymentEvent.builder()
                .userId(cmd.userId().value())
                .accountId(cmd.accountId().value())
                .amount(saved.amount().negate())
                .currency(loan.details().currency())
                .description("Loan Payment: " + loan.name() + " (Installment " + saved.installmentNumber() + ")")
                .date(paidDate)
                .build());

        return saved;
    }
}
