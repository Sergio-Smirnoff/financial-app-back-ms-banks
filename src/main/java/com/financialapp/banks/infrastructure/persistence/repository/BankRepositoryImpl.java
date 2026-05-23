package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.BankPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BankRepositoryImpl implements BankRepository {

    private final BankJpaRepository bankJpaRepository;
    private final BankPersistenceMapper mapper;

    @Override
    public List<Bank> findAll() {
        return bankJpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Bank> findByName(BankName name) {
        return bankJpaRepository.findByName(name.name()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(BankName name) {
        return bankJpaRepository.findByName(name.name()).isPresent();
    }

    @Override
    @Transactional
    public Bank save(Bank bank) {
        var entity = bankJpaRepository.findByName(bank.name().name())
                .map(existing -> mapper.merge(existing, bank))
                .orElseGet(() -> mapper.toJpa(bank));
        return mapper.toDomain(bankJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(BankName name) {
        bankJpaRepository.findByName(name.name()).ifPresent(bankJpaRepository::delete);
    }
}
