package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.CreateAccountCommand;
import com.financialapp.banks.application.account.usecase.CreateAccountUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
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
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + cmd.bankName()));

        if (accountRepository.existsByBankNameAndName(cmd.bankName(), cmd.name())) {
            throw new BusinessException("Account '" + cmd.name() + "' already exists in this bank");
        }

        if (AccountType.INVESTMENT.name().equals(cmd.type()) &&
                accountRepository.existsByBankNameAndTypeAndCurrency(
                        cmd.bankName(), AccountType.INVESTMENT.name(), cmd.initialBalance().currency())) {
            throw new BusinessException("Investment account in " + cmd.initialBalance().currency() + " already exists for this bank");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = cmd.isActive() != null ? cmd.isActive() : true;
        Account account = switch (cmd.type()) {
            case "CHECKING" -> new CheckingAccount(cmd.cbu(), cmd.alias(), cmd.initialBalance(),
                    cmd.userId(), cmd.bankName(), cmd.name(), isActive, now, now);
            case "SAVINGS" -> new SavingsAccount(cmd.cbu(), cmd.alias(), cmd.initialBalance(),
                    cmd.userId(), cmd.bankName(), cmd.name(), isActive, now, now);
            case "INVESTMENT" -> new InvestmentAccount(cmd.cbu(), cmd.alias(), cmd.initialBalance(),
                    cmd.userId(), cmd.bankName(), cmd.name(), isActive, now, now);
            default -> throw new BusinessException("Unknown account type: " + cmd.type());
        };

        return accountRepository.save(account);
    }
}
