package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findByUserIdOrderByNameAsc(Long userId);
    List<AccountJpaEntity> findByBank_IdOrderByNameAsc(Long bankId);
    Optional<AccountJpaEntity> findByCbu(String cbu);
    Optional<AccountJpaEntity> findByAliasAndBank_BankNumber(String alias, String bankNumber);
    boolean existsByUserIdAndBank_BankNumberAndName(Long userId, String bankNumber, String name);
    boolean existsByUserIdAndBank_BankNumberAndTypeAndCurrency(Long userId, String bankNumber, String type, String currency);
    int countByBank_BankNumber(String bankNumber);
    void deleteByCbu(String cbu);

    @Query("SELECT a FROM AccountJpaEntity a WHERE a.isActive = true AND a.balance < :threshold")
    List<AccountJpaEntity> findLowBalanceAccounts(@Param("threshold") BigDecimal threshold);

    @Query("SELECT a FROM AccountJpaEntity a WHERE a.userId = :userId " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:currency IS NULL OR a.currency = :currency) " +
           "AND (:bankNumber IS NULL OR a.bank.bankNumber = :bankNumber) " +
           "AND (:name IS NULL OR a.name = :name) " +
           "AND (:hideEmpty = false OR a.balance > 0) " +
           "ORDER BY a.name ASC")
    List<AccountJpaEntity> findFiltered(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("currency") String currency,
            @Param("bankNumber") String bankNumber,
            @Param("name") String name,
            @Param("hideEmpty") boolean hideEmpty);
}
