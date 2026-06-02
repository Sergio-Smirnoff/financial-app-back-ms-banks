package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.CardJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.CardInstallmentPersistenceMapper;
import com.financialapp.banks.infrastructure.persistence.mapper.CardPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final CardJpaRepository cardJpaRepository;
    private final CardInstallmentJpaRepository cardInstallmentJpaRepository;
    private final BankJpaRepository bankJpaRepository;
    private final CardPersistenceMapper mapper;
    private final CardInstallmentPersistenceMapper installmentMapper;

    @Override
    public List<Card> findByUserId(UserId userId) {
        return cardJpaRepository.findByUserId(userId.value())
                .stream().map(this::loadDomain).toList();
    }

    @Override
    public List<Card> findByBankNumber(BankNumber bankNumber) {
        BankJpaEntity bank = requireBank(bankNumber);
        return cardJpaRepository.findByBankId(bank.getId())
                .stream().map(entity -> mapper.toDomain(entity, bank)).toList();
    }

    @Override
    public int countByBankNumber(BankNumber bankNumber) {
        return bankJpaRepository.findByBankNumber(bankNumber.value())
                .map(bank -> cardJpaRepository.countByBankId(bank.getId())).orElse(0);
    }

    @Override
    public Optional<Card> findByCardNumber(String cardNumber) {
        return cardJpaRepository.findByCardNumber(cardNumber).map(this::loadDomain);
    }

    @Override
    public Optional<Card> findByCardNumberAndUserId(String cardNumber, UserId userId) {
        return cardJpaRepository.findByCardNumberAndUserId(cardNumber, userId.value()).map(this::loadDomain);
    }

    @Override
    public boolean existsByBankNumberAndBrandAndTypeAndCardNumber(BankNumber bankNumber, CardBrand brand, CardType type, String cardNumber) {
        return bankJpaRepository.findByBankNumber(bankNumber.value())
                .map(bank -> cardJpaRepository.existsByBankIdAndBrandAndCardTypeAndCardNumber(
                        bank.getId(), brand, type, cardNumber))
                .orElse(false);
    }

    @Override
    public List<Card> findExpiringBetween(LocalDate from, LocalDate to) {
        return cardJpaRepository.findExpiringBetween(from, to)
                .stream().map(this::loadDomain).toList();
    }

    @Override
    public List<CardInstallment> findUpcomingUnpaidInstallments(UserId userId, LocalDate from, LocalDate to) {
        return cardInstallmentJpaRepository.findUpcomingUnpaid(userId.value(), from, to)
                .stream().map(installmentMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Card save(Card card) {
        BankJpaEntity bank = requireBank(card.bankNumber());
        CardJpaEntity entity = cardJpaRepository.findByCardNumber(card.cardNumber().value())
                .map(existing -> mapper.merge(existing, card, bank))
                .orElseGet(() -> mapper.toJpa(card, bank));
        return mapper.toDomain(cardJpaRepository.save(entity), bank);
    }

    @Override
    @Transactional
    public void delete(String cardNumber) {
        cardJpaRepository.deleteByCardNumber(cardNumber);
    }

    private Card loadDomain(CardJpaEntity entity) {
        BankJpaEntity bank = bankJpaRepository.findById(entity.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", entity.getBankId().toString()));
        return mapper.toDomain(entity, bank);
    }

    private BankJpaEntity requireBank(BankNumber bankNumber) {
        return bankJpaRepository.findByBankNumber(bankNumber.value())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", bankNumber.value()));
    }
}
