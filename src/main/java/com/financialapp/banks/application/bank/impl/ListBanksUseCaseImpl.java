package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.domain.usecase.bank.BankWithAccounts;
import com.financialapp.banks.domain.usecase.bank.ListBanksUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListBanksUseCaseImpl implements ListBanksUseCase {

    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BankWithAccounts> execute(UserId userId) {
        Map<BankNumber, List<Account>> byBank = accountRepository.findByUserId(userId).stream()
                .collect(Collectors.groupingBy(Account::bankNumber));

        return byBank.entrySet().stream()
                .map(bankEntry -> {
                    Bank bank = bankRepository.findByBankNumber(bankEntry.getKey())
                            .orElseThrow(() -> new ResourceNotFoundException("Bank", bankEntry.getKey().value()));
                    return new BankWithAccounts(bank, bankEntry.getValue());
                })
                .sorted(Comparator.comparing(bankWithAccounts -> bankWithAccounts.bank().name()))
                .toList();
    }
}
