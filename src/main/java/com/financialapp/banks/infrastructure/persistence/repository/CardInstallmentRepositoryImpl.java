package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.repository.CardInstallmentRepository;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.CardJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.CardInstallmentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardInstallmentRepositoryImpl implements CardInstallmentRepository {

    private final CardInstallmentJpaRepository jpaRepository;
    private final CardJpaRepository cardJpaRepository;
    private final CardInstallmentPersistenceMapper mapper;

    @Override
    public List<CardInstallment> findByParentId(String cardNumber) {
        return findByCardNumber(cardNumber);
    }

    @Override
    public List<CardInstallment> findByCardNumber(String cardNumber) {
        return jpaRepository.findByCard_CardNumberOrderByDueDateAsc(cardNumber)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<CardInstallment> findById(CardInstallmentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByParentIdAndUnpaid(String cardNumber) {
        return existsByCardNumberAndUnpaid(cardNumber);
    }

    @Override
    public boolean existsByCardNumberAndUnpaid(String cardNumber) {
        return jpaRepository.existsByCard_CardNumberAndPaidFalse(cardNumber);
    }

    @Override
    public boolean existsByParentIdAndDescriptionAndAmountAndDueDate(String cardNumber, String description, Money amount, LocalDate dueDate) {
        return existsByCardNumberAndDescriptionAndAmountAndDueDate(cardNumber, description, amount, dueDate);
    }

    @Override
    public boolean existsByCardNumberAndDescriptionAndAmountAndDueDate(String cardNumber, String description, Money amount, LocalDate dueDate) {
        return jpaRepository.existsByCard_CardNumberAndDescriptionAndAmountAndDueDate(
                cardNumber, description, amount.amount(), dueDate);
    }

    @Override
    @Transactional
    public CardInstallment save(CardInstallment installment) {
        CardJpaEntity card = cardJpaRepository.findByCardNumber(installment.cardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + installment.cardNumber()));
        var entity = installment.id() != null
                ? jpaRepository.findById(installment.id().value())
                        .map(existing -> mapper.merge(existing, installment, card))
                        .orElseGet(() -> mapper.toJpa(installment, card))
                : mapper.toJpa(installment, card);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public List<CardInstallment> saveAll(List<CardInstallment> installments) {
        return installments.stream().map(this::save).toList();
    }

    @Override
    public List<CardInstallment> findUpcomingUnpaid(UserId userId, LocalDate from, LocalDate to) {
        return jpaRepository.findUpcomingUnpaid(userId.value(), from, to)
                .stream().map(mapper::toDomain).toList();
    }
}
