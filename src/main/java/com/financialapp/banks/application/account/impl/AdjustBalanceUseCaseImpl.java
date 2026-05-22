package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.usecase.AdjustBalanceUseCase;
import com.financialapp.banks.domain.event.BalanceAdjustedEvent;
import com.financialapp.banks.domain.event.LowBalanceEvent;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
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
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + cmd.accountCbu()));

        if (account.type() == AccountType.INVESTMENT) {
            throw new BusinessException("Cannot manually adjust balance of an investment account");
        }

        if (cmd.delta().currency() != null &&
                !account.balance().currency().equals(cmd.delta().currency())) {
            throw new BusinessException("Currency mismatch: account is " + account.balance().currency() +
                    " but operation is " + cmd.delta().currency());
        }

        BigDecimal newBalance = account.balance().amount().add(cmd.delta().amount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient funds in account: " + account.name());
        }

        Money updatedBalance = new Money(newBalance, account.balance().currency());
        Account updated = buildAccount(account, updatedBalance);
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

    private Account buildAccount(Account original, Money newBalance) {
        LocalDateTime now = LocalDateTime.now();
        return switch (original.type()) {
            case CHECKING -> new CheckingAccount(original.cbu(), original.alias(), newBalance,
                    original.userId(), original.bankName(), original.name(), original.isActive(),
                    original.createdAt(), now);
            case SAVINGS -> new SavingsAccount(original.cbu(), original.alias(), newBalance,
                    original.userId(), original.bankName(), original.name(), original.isActive(),
                    original.createdAt(), now);
            case INVESTMENT -> new InvestmentAccount(original.cbu(), original.alias(), newBalance,
                    original.userId(), original.bankName(), original.name(), original.isActive(),
                    original.createdAt(), now);
        };
    }
}
