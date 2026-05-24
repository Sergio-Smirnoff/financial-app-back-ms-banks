package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.command.DeleteBankCommand;
import com.financialapp.banks.application.bank.usecase.DeleteBankUseCase;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ResourceConflictException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeleteBankUseCaseImpl implements DeleteBankUseCase {

    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void execute(DeleteBankCommand command) {
        Bank bank = bankRepository.findByName(command.name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", command.name().getDisplayName()));

        List<Account> accounts = accountRepository.findByBankName(bank.name());
        long activeCount = accounts.stream().filter(a -> Boolean.TRUE.equals(a.isActive())).count();
        if (activeCount > 0) {
            throw new ResourceConflictException(
                DomainError.BANK_HAS_ACTIVE_ACCOUNTS,
                "Cannot delete bank '" + command.name().getDisplayName() + "' — it has " + activeCount + " active account(s)",
                Map.of("bankName", command.name().getDisplayName(), "activeAccounts", activeCount));
        }

        bankRepository.delete(bank.name());
    }
}
