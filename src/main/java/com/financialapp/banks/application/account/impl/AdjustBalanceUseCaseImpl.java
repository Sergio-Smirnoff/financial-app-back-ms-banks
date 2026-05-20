package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;
import com.financialapp.banks.application.account.usecase.AdjustBalanceUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountDetails;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.port.DomainEventPublisher;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.infrastructure.messaging.payload.PaymentEvent;
import com.financialapp.banks.domain.common.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        if (cmd.expectedCurrency() != null &&
                !account.details().balance().currency().equals(cmd.expectedCurrency())) {
            throw new BusinessException("Currency mismatch: account is " + account.details().balance() +
                    " but operation is " + cmd.expectedCurrency());
        }

        BigDecimal newBalance = account.details().balance().amount().add(cmd.delta());

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
                java.time.LocalDateTime.now()
        );
        accountRepository.save(updated);

        if (newBalance.compareTo(LOW_BALANCE_THRESHOLD) < 0) {
            eventPublisher.publish(BankAlertEvent.builder()
                    .userId(account.userId().value())
                    .type("LOW_BALANCE")
                    .title("Low Account Balance")
                    .message(String.format("Account '%s' has a low balance of %.2f %s.",
                            account.details().name(), newBalance, account.details().balance().currency()))
                    .metadata(String.format("{\"accountId\":%d,\"bankName\":%s,\"balance\":%.2f}",
                            account.id().value(), account.bankName(), newBalance))
                    .build());
        }

        String type = cmd.delta().compareTo(BigDecimal.ZERO) >= 0 ? "TRANSFER_RECEIVED" : "TRANSFER_SENT";
        String title = cmd.delta().compareTo(BigDecimal.ZERO) >= 0 ? "Funds Received" : "Funds Sent";
        String action = cmd.delta().compareTo(BigDecimal.ZERO) >= 0 ? "credited to" : "debited from";

        eventPublisher.publish(BankAlertEvent.builder()
                .userId(account.userId().value())
                .type(type)
                .title(title)
                .message(String.format("%.2f %s has been %s account '%s'.",
                        cmd.delta().abs(), account.details().balance().currency(), action, account.details().name()))
                .metadata(String.format("{\"accountId\":%d,\"bankName\":%s,\"amount\":%.2f}",
                        account.id().value(), account.bankName(), cmd.delta().abs()))
                .build());
    }
}
