package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.AccountFeeScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountFeeScheduleJpaRepository extends JpaRepository<AccountFeeScheduleJpaEntity, Long> {
    Optional<AccountFeeScheduleJpaEntity> findByAccountCbu(String accountCbu);

    @Query("SELECT s FROM AccountFeeScheduleJpaEntity s JOIN AccountJpaEntity a ON s.accountCbu = a.cbu WHERE a.userId = :userId")
    List<AccountFeeScheduleJpaEntity> findByUserId(@Param("userId") Long userId);
}
