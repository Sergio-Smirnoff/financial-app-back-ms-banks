package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises BankRepositoryImpl + BankPersistenceMapper against the seeded H2 catalog. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankRepositoryIT {

    @Autowired BankRepository bankRepository;

    @Test
    void findByBankNumber_returnsSeededBank() {
        // Given the catalog seeder ran at startup / When looking up a known bank
        Optional<Bank> bank = bankRepository.findByBankNumber(new BankNumber("007"));

        // Then it is found and mapped to the domain
        assertThat(bank).isPresent();
        assertThat(bank.get().name()).isEqualTo("GALICIA");
    }

    @Test
    void existsByBankNumber_reflectsCatalog() {
        // Given the seeded catalog / When checking existence
        // Then a seeded bank exists and an unseeded one does not
        assertThat(bankRepository.existsByBankNumber(new BankNumber("007"))).isTrue();
        assertThat(bankRepository.existsByBankNumber(new BankNumber("999"))).isFalse();
    }

    @Test
    void findAll_returnsSeededCatalog() {
        // Given the seeded catalog / When listing all banks / Then every entry is mapped
        assertThat(bankRepository.findAll()).isNotEmpty();
    }
}
