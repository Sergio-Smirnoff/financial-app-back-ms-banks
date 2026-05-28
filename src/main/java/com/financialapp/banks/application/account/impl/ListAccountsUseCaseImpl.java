package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.FilterAccountCommand;
import com.financialapp.banks.domain.usecase.account.ListAccountsUseCase;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAccountsUseCaseImpl implements ListAccountsUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Account> execute(FilterAccountCommand command) {
        return accountRepository.findFiltered(
                command.userId(),
                command.type(),
                command.currency(),
                command.bankName(),
                command.name(),
                command.hideEmpty()
        );
    }
}
