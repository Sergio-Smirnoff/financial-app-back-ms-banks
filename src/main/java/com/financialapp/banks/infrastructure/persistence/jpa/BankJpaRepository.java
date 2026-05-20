package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {
    List<Bank> findByUserIdOrderByNameAsc(Long userId);
    Optional<Bank> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
