package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.usecase.BankSummary;
import com.financialapp.banks.application.bank.usecase.ListBanksUseCase;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ListBanksUseCaseImpl implements ListBanksUseCase {

    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BankSummary> execute() {
        return bankRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private BankSummary toSummary(Bank bank) {
        List<Account> accounts = accountRepository.findByBankName(bank.name());
        int cardsCount = cardRepository.countByBankName(bank.name());
        int loansCount = loanRepository.countByBankName(bank.name());

        Map<Currency, BigDecimal> totals = new HashMap<>();
        for (Account a : accounts) {
            totals.merge(a.balance().currency(), a.balance().amount(), BigDecimal::add);
        }

        return new BankSummary(bank, accounts.size(), cardsCount, loansCount, totals);
    }
}
