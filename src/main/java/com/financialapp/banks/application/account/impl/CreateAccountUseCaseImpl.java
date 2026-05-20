package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.CreateAccountCommand;
import com.financialapp.banks.application.account.usecase.CreateAccountUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountDetails;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.account.AccountInformation;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.common.model.Money;

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
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + cmd.bankName()));

        if (accountRepository.existsByBankNameAndName(cmd.bankName(), cmd.name())) {
            throw new BusinessException("Account '" + cmd.name() + "' already exists in this bank");
        }

        if (cmd.type() == AccountType.INVESTMENT &&
                accountRepository.existsByBankNameAndTypeAndCurrency(cmd.bankName(), AccountType.INVESTMENT, cmd.initialBalance().currency())) {
            throw new BusinessException("Investment account in " + cmd.initialBalance().currency() + " already exists for this bank");
        }

        Account account = new Account(
                new AccountInformation(cmd.cbu(), cmd.alias()),
                new AccountId(null),
                cmd.userId(),
                cmd.bankName(),
                new AccountDetails(
                        cmd.name(),
                        cmd.type(),
                        new Money(cmd.initialBalance().amount(), cmd.initialBalance().currency()),
                        true
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return accountRepository.save(account);
    }
}
