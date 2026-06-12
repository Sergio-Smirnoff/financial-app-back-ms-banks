package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.CloseAccountCommand;
import com.financialapp.banks.domain.usecase.account.CloseAccountUseCase;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ResourceConflictException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloseAccountUseCaseImpl implements CloseAccountUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void execute(CloseAccountCommand command) {
        Account account = accountRepository.findByCbu(command.cbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", command.cbu()));

        if (account.balance().amount().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResourceConflictException(
                DomainError.ACCOUNT_NOT_DELETABLE,
                "Cannot delete account '" + command.cbu() + "': non-zero balance",
                Map.of("cbu", command.cbu(), "reason", "non-zero balance"));
        }

        accountRepository.delete(account.cbu().value());
    }
}
