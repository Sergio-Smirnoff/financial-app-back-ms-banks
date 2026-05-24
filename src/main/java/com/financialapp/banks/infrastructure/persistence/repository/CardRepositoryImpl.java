package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.CardJpaRepository;
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
    private final BankJpaRepository bankJpaRepository;
    private final CardPersistenceMapper mapper;

    @Override
    public List<Card> findByUserId(UserId userId) {
        return cardJpaRepository.findByUserId(userId.value())
                .stream().map(this::loadDomain).toList();
    }

    @Override
    public List<Card> findByBankName(BankName bankName) {
        BankJpaEntity bank = requireBank(bankName);
        return cardJpaRepository.findByBankId(bank.getId())
                .stream().map(entity -> mapper.toDomain(entity, bank)).toList();
    }

    @Override
    public int countByBankName(BankName bankName) {
        return bankJpaRepository.findByName(bankName.name())
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
    public boolean existsByBankNameAndBrandAndTypeAndCardNumber(BankName bankName, CardBrand brand, CardType type, String cardNumber) {
        return bankJpaRepository.findByName(bankName.name())
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
    @Transactional
    public Card save(Card card) {
        BankJpaEntity bank = requireBank(card.bankName());
        CardJpaEntity entity = cardJpaRepository.findByCardNumber(card.cardNumber())
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
                .orElseThrow(() -> new ResourceNotFoundException("Bank", entity.getCardNumber()));
        return mapper.toDomain(entity, bank);
    }

    private BankJpaEntity requireBank(BankName name) {
        return bankJpaRepository.findByName(name.name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", name.getDisplayName()));
    }
}
