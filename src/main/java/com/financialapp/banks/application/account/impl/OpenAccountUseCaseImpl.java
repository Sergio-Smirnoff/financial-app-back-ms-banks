package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.domain.usecase.account.command.OpenAccountCommand;
import com.financialapp.banks.domain.usecase.account.OpenAccountUseCase;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.exception.cbu.CbuBankMismatchException;
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
public class OpenAccountUseCaseImpl implements OpenAccountUseCase {

    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;

    @Override
    @Transactional
    public Account execute(OpenAccountCommand cmd) {
        bankRepository.findByBankNumber(cmd.bankNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", cmd.bankNumber().value()));

        Cbu cbu = Cbu.from(cmd.cbu());
        if (!cbu.bankNumber().equals(cmd.bankNumber())) {
            throw new CbuBankMismatchException(cmd.bankNumber().value(), cbu.bankNumber().value());
        }

        if (accountRepository.existsByUserIdAndBankNumberAndName(cmd.userId(), cmd.bankNumber(), cmd.name())) {
            throw new ResourceAlreadyExistsException("Account", cmd.name() + " in bank " + cmd.bankNumber().value());
        }

        if (cmd.type() == AccountType.INVESTMENT &&
                accountRepository.existsByUserIdAndBankNumberAndTypeAndCurrency(
                        cmd.userId(), cmd.bankNumber(), AccountType.INVESTMENT.name(), cmd.initialBalance().currency())) {
            throw new ResourceAlreadyExistsException("InvestmentAccount", cmd.initialBalance().currency() + " in bank " + cmd.bankNumber().value());
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = cmd.isActive() != null ? cmd.isActive() : true;
        Account account = Account.create(cmd.type(), cbu, cmd.alias(), cmd.initialBalance(),
                cmd.userId(), cmd.bankNumber(), cmd.name(), isActive, now, now);

        return accountRepository.save(account);
    }
}
