package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.UpdateAccountCommand;
import com.financialapp.banks.application.account.usecase.UpdateAccountUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateAccountUseCaseImpl implements UpdateAccountUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Account execute(UpdateAccountCommand cmd) {
        Account existing = accountRepository.findByCbu(cmd.cbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + cmd.cbu()));

        if (cmd.name() != null && !existing.name().equals(cmd.name()) &&
                accountRepository.existsByBankNameAndName(existing.bankName(), cmd.name())) {
            throw new BusinessException("Account '" + cmd.name() + "' already exists in this bank");
        }

        String newName = cmd.name() != null ? cmd.name() : existing.name();
        Money newBalance = cmd.balance() != null ? cmd.balance() : existing.balance();
        Boolean newActive = cmd.isActive() != null ? cmd.isActive() : existing.isActive();
        LocalDateTime now = LocalDateTime.now();

        Account updated = switch (existing) {
            case CheckingAccount ignored -> new CheckingAccount(existing.cbu(), existing.alias(), newBalance,
                    existing.userId(), existing.bankName(), newName, newActive, existing.createdAt(), now);
            case SavingsAccount ignored -> new SavingsAccount(existing.cbu(), existing.alias(), newBalance,
                    existing.userId(), existing.bankName(), newName, newActive, existing.createdAt(), now);
            case InvestmentAccount ignored -> new InvestmentAccount(existing.cbu(), existing.alias(), newBalance,
                    existing.userId(), existing.bankName(), newName, newActive, existing.createdAt(), now);
            default -> throw new IllegalStateException("Unknown account subtype: " + existing.getClass().getSimpleName());
        };

        return accountRepository.save(updated);
    }
}
