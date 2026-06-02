package com.financialapp.banks.infrastructure.persistence.seed;

import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankCatalogSeederTest {

    @Mock BankJpaRepository bankJpaRepository;
    BankCatalogSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new BankCatalogSeeder(bankJpaRepository);
    }

    @Test
    void run_seedsAllMissingBanks() {
        // Given no banks exist yet
        when(bankJpaRepository.findByBankNumber(anyString())).thenReturn(Optional.empty());

        // When the seeder runs
        seeder.run(null);

        // Then one entity is saved per catalog entry (13 BCRA banks)
        verify(bankJpaRepository, times(13)).save(any(BankJpaEntity.class));
    }

    @Test
    void run_savesNothing_whenAllBanksAlreadyPresent() {
        // Given every bank already exists (the empty-check false branch)
        when(bankJpaRepository.findByBankNumber(anyString()))
                .thenReturn(Optional.of(BankJpaEntity.builder().build()));

        // When the seeder runs
        seeder.run(null);

        // Then nothing is saved (created stays zero)
        verify(bankJpaRepository, never()).save(any());
    }
}
