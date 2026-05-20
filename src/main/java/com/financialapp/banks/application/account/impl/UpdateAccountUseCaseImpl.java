package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.UpdateAccountCommand;
import com.financialapp.banks.application.account.usecase.UpdateAccountUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountDetails;
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
        Account existing = accountRepository.findById(cmd.id())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + cmd.id().value()));

        if (!existing.details().name().equals(cmd.name()) &&
                accountRepository.existsByBankNameAndName(existing.bankName(), cmd.name())) {
            throw new BusinessException("Account '" + cmd.name() + "' already exists in this bank");
        }

        Account updated = new Account(
                existing.information(),
                existing.id(),
                existing.userId(),
                existing.bankName(),
                new AccountDetails(
                        cmd.name(),
                        existing.details().type(),
                        cmd.balance(),
                        cmd.isActive() != null ? cmd.isActive() : existing.details().isActive()
                ),
                existing.createdAt(),
                LocalDateTime.now()
        );

        return accountRepository.save(updated);
    }
}
