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
import com.financialapp.banks.domain.model.loan.LoanOrigination;
import com.financialapp.banks.domain.gateway.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        bankRepository.findByBankNumber(cmd.bankNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankNumber().value()));

        Account dest = accountRepository.findByCbu(cmd.destinationAccountCbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", cmd.destinationAccountCbu()));

        if (!dest.bankNumber().equals(cmd.bankNumber())) {
            throw new LoanAccountMismatchException(cmd.destinationAccountCbu(), cmd.bankNumber().value());
        }

        Currency currency = dest.balance().currency();
        BigDecimal principal = new BigDecimal(cmd.principal());
        BigDecimal interestRate = new BigDecimal(cmd.interestRate());
        LoanOrigination origination = Loan.originate(
                cmd.userId(), cmd.bankNumber(), cmd.name(),
                new Money(principal, currency), interestRate,
                cmd.totalInstallments(), cmd.amortizationType(), cmd.startDate(),
                cmd.destinationAccountCbu());

        Loan saved = loanRepository.save(origination.loan());

        adjustBalance.execute(new AdjustBalanceCommand(
                cmd.destinationAccountCbu(), new Money(principal, currency)));

        eventPublisher.publishAll(origination.events());

        return saved;
    }
}
