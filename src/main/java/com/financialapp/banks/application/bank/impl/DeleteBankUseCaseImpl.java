package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.command.DeleteBankCommand;
import com.financialapp.banks.application.bank.usecase.DeleteBankUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteBankUseCaseImpl implements DeleteBankUseCase {

    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void execute(DeleteBankCommand command) {
        Bank bank = bankRepository.findByName(command.name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + command.name()));

        List<Account> accounts = accountRepository.findByBankName(bank.name());
        boolean hasActiveAccounts = accounts.stream().anyMatch(a -> Boolean.TRUE.equals(a.isActive()));
        if (hasActiveAccounts) {
            throw new BusinessException("Cannot delete bank with active accounts. Deactivate or delete all accounts first.");
        }

        bankRepository.delete(bank.name());
    }
}
