package com.financialapp.banks.application.loan.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.usecase.loan.command.OriginateLoanCommand;
import com.financialapp.banks.domain.usecase.loan.OriginateLoanUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.loan.LoanAccountMismatchException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.event.LoanCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Currency;

@Service
@RequiredArgsConstructor
public class OriginateLoanUseCaseImpl implements OriginateLoanUseCase {

    private final LoanRepository loanRepository;
    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final AdjustBalanceUseCase adjustBalance;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Loan execute(OriginateLoanCommand cmd) {
        bankRepository.findByName(cmd.bankName())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankName().getDisplayName()));

        Account dest = accountRepository.findByCbu(cmd.destinationAccountCbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", cmd.destinationAccountCbu()));

        if (!dest.bankName().equals(cmd.bankName())) {
            throw new LoanAccountMismatchException(cmd.destinationAccountCbu(), cmd.bankName().getDisplayName());
        }

        Currency currency = dest.balance().currency();
        Loan loan = Loan.originate(
                cmd.userId(), cmd.bankName(), cmd.name(),
                new Money(cmd.principal(), currency), cmd.interestRate(),
                cmd.totalInstallments(), cmd.amortizationType(), cmd.startDate());

        Loan saved = loanRepository.save(loan);

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.destinationAccountCbu(), new Money(cmd.principal(), currency)));

        eventPublisher.publish(new LoanCreatedEvent(
                cmd.userId(), cmd.destinationAccountCbu(),
                new Money(cmd.principal(), currency), cmd.name(), LocalDate.now()));

        return saved;
    }
}
