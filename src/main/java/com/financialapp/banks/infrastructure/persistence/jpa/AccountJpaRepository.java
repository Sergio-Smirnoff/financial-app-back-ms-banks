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
    Optional<AccountJpaEntity> findByCbuAndBank_Name(String cbu, String bankName);
    Optional<AccountJpaEntity> findByAliasAndBank_Name(String alias, String bankName);
    boolean existsByBank_NameAndName(String bankName, String name);
    boolean existsByBank_NameAndTypeAndCurrency(String bankName, String type, String currency);
    int countByBank_Name(String bankName);
    void deleteByCbu(String cbu);

    @Query("SELECT a FROM AccountJpaEntity a WHERE a.isActive = true AND a.balance < :threshold")
    List<AccountJpaEntity> findLowBalanceAccounts(@Param("threshold") BigDecimal threshold);

    @Query("SELECT a FROM AccountJpaEntity a WHERE a.userId = :userId " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:currency IS NULL OR UPPER(a.currency) = UPPER(:currency)) " +
           "AND (:bankName IS NULL OR a.bank.name = :bankName) " +
           "AND (:name IS NULL OR a.name = :name) " +
           "AND (:hideEmpty = false OR a.balance > 0) " +
           "ORDER BY a.name ASC")
    List<AccountJpaEntity> findFiltered(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("currency") String currency,
            @Param("bankName") String bankName,
            @Param("name") String name,
            @Param("hideEmpty") boolean hideEmpty);
}
