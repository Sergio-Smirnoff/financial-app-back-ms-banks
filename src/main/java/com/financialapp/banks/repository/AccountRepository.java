package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByNameAsc(Long userId);
    List<Account> findByBankIdOrderByNameAsc(Long bankId);
    Optional<Account> findByIdAndUserId(Long id, Long userId);
    boolean existsByBankIdAndName(Long bankId, String name);
    boolean existsByBankIdAndType(Long bankId, com.financialapp.banks.model.enums.AccountType type);
    boolean existsByBankIdAndTypeAndCurrency(Long bankId, com.financialapp.banks.model.enums.AccountType type, String currency);

    @Query("SELECT a FROM Account a WHERE a.isActive = true AND a.balance < :threshold")
    List<Account> findLowBalanceAccounts(@Param("threshold") BigDecimal threshold);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:currency IS NULL OR UPPER(a.currency) = UPPER(:currency)) " +
           "ORDER BY a.name ASC")
    List<Account> findFiltered(
            @Param("userId") Long userId,
            @Param("type") com.financialapp.banks.model.enums.AccountType type,
            @Param("currency") String currency);
}
