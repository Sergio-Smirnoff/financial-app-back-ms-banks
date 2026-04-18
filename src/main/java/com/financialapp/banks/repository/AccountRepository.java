package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByNameAsc(Long userId);
    List<Account> findByBankIdOrderByNameAsc(Long bankId);
    Optional<Account> findByIdAndUserId(Long id, Long userId);
    boolean existsByBankIdAndName(Long bankId, String name);
}
