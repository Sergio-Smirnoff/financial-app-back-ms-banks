package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.usecase.AdjustBalanceUseCase;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountDetails;
import com.financialapp.banks.domain.model.account.AccountType;
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
        Account account = accountRepository.findById(cmd.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + cmd.accountId().value()));

        if (account.details().type() == AccountType.INVESTMENT) {
            throw new BusinessException("Cannot manually adjust balance of an investment account");
        }

        if (cmd.delta().currency() != null &&
                !account.details().balance().currency().equals(cmd.delta().currency())) {
            throw new BusinessException("Currency mismatch: account is " + account.details().balance().currency() +
                    " but operation is " + cmd.delta().currency());
        }

        BigDecimal newBalance = account.details().balance().amount().add(cmd.delta().amount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient funds in account: " + account.details().name());
        }

        AccountDetails updatedDetails = new AccountDetails(
                account.details().name(),
                account.details().type(),
                new Money(newBalance, account.details().balance().currency()),
                account.details().isActive()
        );
        Account updated = new Account(
                account.information(),
                account.id(),
                account.userId(),
                account.bankName(),
                updatedDetails,
                account.createdAt(),
                LocalDateTime.now()
        );
        accountRepository.save(updated);

        if (newBalance.compareTo(LOW_BALANCE_THRESHOLD) < 0) {
            eventPublisher.publish(new LowBalanceEvent(
                    account.userId(),
                    account.id(),
                    account.bankName(),
                    account.details().name(),
                    account.details().balance()
            ));
        }

        eventPublisher.publish(new BalanceAdjustedEvent(
                account.userId(),
                account.id(),
                account.bankName(),
                account.details().name(),
                cmd.delta()
        ));
    }
}
