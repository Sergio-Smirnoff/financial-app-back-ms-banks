package com.financialapp.banks.infrastructure.persistence.seed;

import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankCatalogSeeder implements ApplicationRunner {

    private final BankJpaRepository bankJpaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int created = 0;
        for (BankName name : BankName.values()) {
            if (bankJpaRepository.findByName(name.name()).isEmpty()) {
                bankJpaRepository.save(BankJpaEntity.builder().name(name.name()).build());
                created++;
            }
        }
        if (created > 0) {
            log.info("Bank catalog seeded: {} new bank(s)", created);
        }
    }
}
