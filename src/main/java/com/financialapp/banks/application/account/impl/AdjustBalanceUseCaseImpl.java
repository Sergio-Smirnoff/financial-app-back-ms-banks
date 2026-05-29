package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;
import com.financialapp.banks.domain.usecase.account.AdjustBalanceUseCase;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
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

        // Normalize the signed delta to the account currency when none is supplied,
        // preserving the original tolerance for currency-less deltas.
        Money delta = cmd.delta();
        Money amount = delta.currency() == null
                ? new Money(delta.amount(), account.balance().currency())
                : delta;

        // Decide debit vs credit from the signed delta, exactly as before:
        // a negative delta reduces the balance, otherwise it increases it.
        // The Account aggregate enforces the investment restriction, the
        // same-currency guard and the insufficient-funds invariant.
        Account updated = amount.isNegative()
                ? account.debit(amount.negate(), LocalDateTime.now())
                : account.credit(amount, LocalDateTime.now());

        accountRepository.save(updated);

        if (updated.isLowBalance(new Money(LOW_BALANCE_THRESHOLD, account.balance().currency()))) {
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
