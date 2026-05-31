package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.BankPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BankRepositoryImpl implements BankRepository {

    private final BankJpaRepository bankJpaRepository;
    private final BankPersistenceMapper mapper;

    @Override
    public Optional<Bank> findByBankNumber(BankNumber bankNumber) {
        return bankJpaRepository.findByBankNumber(bankNumber.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByBankNumber(BankNumber bankNumber) {
        return bankJpaRepository.existsByBankNumber(bankNumber.value());
    }

    @Override
    public List<Bank> findAll() {
        return bankJpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
