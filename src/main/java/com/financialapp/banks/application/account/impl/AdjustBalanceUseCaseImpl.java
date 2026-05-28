package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.account.AccountCurrencyMismatchException;
import com.financialapp.banks.domain.exception.account.AccountInsufficientFundsException;
import com.financialapp.banks.domain.exception.account.AccountInvestmentRestrictionException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdjustBalanceUseCaseImpl implements AdjustBalanceUseCase {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public void execute(AdjustBalanceCommand cmd) {
        Account account = accountRepository.findByCbu(cmd.accountCbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", cmd.accountCbu()));

        if (account instanceof InvestmentAccount) {
            throw new AccountInvestmentRestrictionException(cmd.accountCbu());
        }

        if (cmd.delta().currency() != null &&
                !account.balance().currency().equals(cmd.delta().currency())) {
            throw new AccountCurrencyMismatchException(
                account.balance().currency().getCurrencyCode(),
                cmd.delta().currency().getCurrencyCode());
        }

        BigDecimal newBalance = account.balance().amount().add(cmd.delta().amount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountInsufficientFundsException(
                cmd.accountCbu(),
                account.balance(),
                new Money(cmd.delta().amount().negate(), account.balance().currency()));
        }

        Money updatedBalance = new Money(newBalance, account.balance().currency());
        Account updated = account.withBalance(updatedBalance, LocalDateTime.now());
        accountRepository.save(updated);

        if (newBalance.compareTo(LOW_BALANCE_THRESHOLD) < 0) {
            eventPublisher.publish(new LowBalanceEvent(
                    account.userId(),
                    account.cbu(),
                    account.bankName(),
                    account.name(),
                    account.balance()
            ));
        }

        eventPublisher.publish(new BalanceAdjustedEvent(
                account.userId(),
                account.cbu(),
                account.bankName(),
                account.name(),
                cmd.delta()
        ));
    }
}
