package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountAdjustment;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.gateway.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdjustBalanceUseCaseImpl implements AdjustBalanceUseCase {

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public void execute(AdjustBalanceCommand cmd) {
        Account account = accountRepository.findByCbu(cmd.accountCbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", cmd.accountCbu()));

        // Normalize the signed delta to the account currency when none is supplied,
        // preserving the original tolerance for currency-less deltas.
        Money delta = cmd.delta();
        Money amount = delta.currency() == null
                ? new Money(delta.amount(), account.balance().currency())
                : delta;

        // A negative delta reduces the balance, otherwise it increases it. The Account
        // aggregate enforces the invariants and records its own balance/low-balance events.
        AccountAdjustment adjustment = amount.isNegative()
                ? account.debit(amount.negate(), LocalDateTime.now())
                : account.credit(amount, LocalDateTime.now());

        accountRepository.save(adjustment.account());
        eventPublisher.publishAll(adjustment.events());
    }
}
