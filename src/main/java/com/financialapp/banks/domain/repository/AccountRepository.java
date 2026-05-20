package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankName;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUserId(UserId userId);
    List<Account> findByBankName(BankName bankName);
    Optional<Account> findById(AccountId id);
    Optional<Account> findByIdAndBankName(AccountId id, BankName bankName);
    boolean existsByBankNameAndName(BankName bankName, String name);
    boolean existsByBankNameAndTypeAndCurrency(BankName bankName, AccountType type, Currency currency);
    List<Account> findLowBalance(BigDecimal threshold);
    List<Account> findFiltered(UserId userId, AccountType type, Currency currency, BankName bankName);
    Account save(Account account);
    void delete(AccountId id);
}
