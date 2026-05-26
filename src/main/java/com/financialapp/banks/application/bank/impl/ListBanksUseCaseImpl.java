package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.usecase.BankWithAccounts;
import com.financialapp.banks.application.bank.usecase.ListBanksUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.domain.repository.AccountRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<BankWithAccounts> execute(UserId userId) {
        Map<BankName, List<Account>> byBank = accountRepository.findByUserId(userId).stream()
                .collect(Collectors.groupingBy(Account::bankName));

        return byBank.entrySet().stream()
                .map(e -> new BankWithAccounts(
                        new Bank(e.getKey(), new Logo(e.getKey().getLogoUrl())),
                        e.getValue()))
                .sorted(Comparator.comparing(b -> b.bank().name().getDisplayName()))
                .toList();
    }
}
