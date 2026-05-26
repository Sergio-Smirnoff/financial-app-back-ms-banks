package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.BankPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BankRepositoryImpl implements BankRepository {

    private final BankJpaRepository bankJpaRepository;
    private final BankPersistenceMapper mapper;

    @Override
    public Optional<Bank> findByName(BankName name) {
        return bankJpaRepository.findByName(name.name()).map(mapper::toDomain);
    }
}
