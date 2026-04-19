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

    @Query("SELECT a FROM Account a WHERE a.isActive = true AND a.balance < :threshold")
    List<Account> findLowBalanceAccounts(@Param("threshold") BigDecimal threshold);
}
