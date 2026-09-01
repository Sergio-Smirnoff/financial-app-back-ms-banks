package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.repository.CardFeeScheduleRepository;
import com.financialapp.banks.infrastructure.persistence.entity.CardFeeScheduleJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.CardFeeScheduleJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.CardFeeSchedulePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardFeeScheduleRepositoryImpl implements CardFeeScheduleRepository {

    private final CardFeeScheduleJpaRepository jpaRepository;
    private final CardFeeSchedulePersistenceMapper mapper;

    @Override
    @Transactional
    public CardFeeSchedule save(CardFeeSchedule schedule) {
        CardFeeScheduleJpaEntity entity = jpaRepository.findByCardNumber(schedule.cardNumber().value())
                .map(existing -> mapper.merge(existing, schedule))
                .orElseGet(() -> mapper.toJpa(schedule));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<CardFeeSchedule> findByCardNumber(CardNumber cardNumber) {
        return jpaRepository.findByCardNumber(cardNumber.value()).map(mapper::toDomain);
    }

    @Override
    public List<CardFeeSchedule> findByOwner(UserId userId) {
        return jpaRepository.findByUserId(userId.value())
                .stream().map(mapper::toDomain).toList();
    }
}
