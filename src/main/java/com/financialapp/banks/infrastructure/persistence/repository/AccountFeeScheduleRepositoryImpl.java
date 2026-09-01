package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.repository.AccountFeeScheduleRepository;
import com.financialapp.banks.infrastructure.persistence.entity.AccountFeeScheduleJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.AccountFeeScheduleJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.AccountFeeSchedulePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountFeeScheduleRepositoryImpl implements AccountFeeScheduleRepository {

    private final AccountFeeScheduleJpaRepository jpaRepository;
    private final AccountFeeSchedulePersistenceMapper mapper;

    @Override
    @Transactional
    public AccountFeeSchedule save(AccountFeeSchedule schedule) {
        AccountFeeScheduleJpaEntity entity = jpaRepository.findByAccountCbu(schedule.accountCbu().value())
                .map(existing -> mapper.merge(existing, schedule))
                .orElseGet(() -> mapper.toJpa(schedule));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<AccountFeeSchedule> findByAccountCbu(Cbu cbu) {
        return jpaRepository.findByAccountCbu(cbu.value()).map(mapper::toDomain);
    }

    @Override
    public List<AccountFeeSchedule> findByOwner(UserId userId) {
        return jpaRepository.findByUserId(userId.value())
                .stream().map(mapper::toDomain).toList();
    }
}
