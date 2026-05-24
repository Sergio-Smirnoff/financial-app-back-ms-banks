package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.impl.AdjustBalanceUseCaseImpl;
import com.financialapp.banks.application.loan.command.CreateLoanCommand;
import com.financialapp.banks.application.loan.usecase.CreateLoanUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.loan.LoanAccountMismatchException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.event.LoanCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateLoanUseCaseImpl implements CreateLoanUseCase {

    private final LoanRepository loanRepository;
    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final AdjustBalanceUseCaseImpl adjustBalance;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Loan execute(CreateLoanCommand cmd) {
        bankRepository.findByName(cmd.bankName())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankName().getDisplayName()));

        Account dest = accountRepository.findByCbu(cmd.destinationAccountCbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", cmd.destinationAccountCbu()));

        if (!dest.bankName().equals(cmd.bankName())) {
            throw new LoanAccountMismatchException(cmd.destinationAccountCbu(), cmd.bankName().getDisplayName());
        }

        Currency currency = dest.balance().currency();

        Loan loan = new Loan(
                new LoanId(null),
                cmd.userId(),
                cmd.bankName(),
                cmd.name(),
                new Money(cmd.principal(), currency),
                cmd.interestRate(),
                cmd.totalInstallments(),
                cmd.totalInstallments(),
                cmd.amortizationType(),
                cmd.startDate(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        loan = loanRepository.save(loan);

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.destinationAccountCbu(), new Money(cmd.principal(), currency)));

        eventPublisher.publish(new LoanCreatedEvent(
                cmd.userId(),
                cmd.destinationAccountCbu(),
                new Money(cmd.principal(), currency),
                cmd.name(),
                LocalDate.now()
        ));

        BigDecimal installmentAmount = calculateFrenchInstallment(
                cmd.principal(), cmd.interestRate(), cmd.totalInstallments());

        List<LoanInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= cmd.totalInstallments(); i++) {
            installments.add(new LoanInstallment(
                    new LoanInstallmentId(null),
                    loan.id(),
                    i,
                    new Money(installmentAmount, currency),
                    cmd.startDate().plusMonths(i - 1),
                    false,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            ));
        }
        installmentRepository.saveAll(installments);

        return loan;
    }

    private BigDecimal calculateFrenchInstallment(BigDecimal principal, BigDecimal annualRate, int n) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        }
        BigDecimal i = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        double r = i.doubleValue();
        double pow = Math.pow(1.0 + r, n);
        return principal.multiply(BigDecimal.valueOf(r * pow / (pow - 1.0)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
