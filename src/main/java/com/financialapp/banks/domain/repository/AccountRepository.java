package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankName;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUserId(UserId userId);
    List<Account> findByBankName(BankName bankName);
    int countByBankName(BankName bankName);
    Optional<Account> findByCbu(String cbu);
    Optional<Account> findByCbuAndBankName(String cbu, BankName bankName);
    Optional<Account> findByAliasAndBankName(String alias, BankName bankName);
    boolean existsByBankNameAndName(BankName bankName, String name);
    boolean existsByBankNameAndTypeAndCurrency(BankName bankName, String type, Currency currency);
    List<Account> findLowBalance(BigDecimal threshold);
    List<Account> findFiltered(UserId userId, String type, Currency currency, BankName bankName, String name, boolean hideEmpty);
    Account save(Account account);
    void delete(String cbu);
}
