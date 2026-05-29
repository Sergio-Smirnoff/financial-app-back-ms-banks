package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.CreateAccountCommand;
import com.financialapp.banks.domain.usecase.account.CreateAccountUseCase;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;

    @Override
    @Transactional
    public Account execute(CreateAccountCommand cmd) {
        bankRepository.findByName(cmd.bankName())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankName().getDisplayName()));

        if (accountRepository.existsByBankNameAndName(cmd.bankName(), cmd.name())) {
            throw new ResourceAlreadyExistsException("Account", cmd.name() + " in " + cmd.bankName().getDisplayName());
        }

        if (cmd.type() == AccountType.INVESTMENT &&
                accountRepository.existsByBankNameAndTypeAndCurrency(
                        cmd.bankName(), AccountType.INVESTMENT.name(), cmd.initialBalance().currency())) {
            throw new ResourceAlreadyExistsException("InvestmentAccount", cmd.initialBalance().currency() + " in " + cmd.bankName().getDisplayName());
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = cmd.isActive() != null ? cmd.isActive() : true;
        Account account = Account.create(cmd.type(), cmd.cbu(), cmd.alias(), cmd.initialBalance(),
                cmd.userId(), cmd.bankName(), cmd.name(), isActive, now, now);

        return accountRepository.save(account);
    }
}
