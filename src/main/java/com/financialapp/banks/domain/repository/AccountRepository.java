package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankNumber;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUserId(UserId userId);
    List<Account> findByBankNumber(BankNumber bankNumber);
    int countByBankNumber(BankNumber bankNumber);
    Optional<Account> findByCbu(String cbu);
    Optional<Account> findByAliasAndBankNumber(String alias, BankNumber bankNumber);
    boolean existsByUserIdAndBankNumberAndName(UserId userId, BankNumber bankNumber, String name);
    List<Account> findLowBalance(BigDecimal threshold);
    List<Account> findFiltered(UserId userId, String type, Currency currency, BankNumber bankNumber, String name, boolean hideEmpty);
    Account save(Account account);
    void delete(String cbu);
}
