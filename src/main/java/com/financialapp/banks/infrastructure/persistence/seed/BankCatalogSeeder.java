package com.financialapp.banks.infrastructure.persistence.seed;

import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Idempotently seeds the bank catalog (BCRA bank number -> name) at startup.
 * In production this mirrors Flyway V13 and is a no-op (rows already exist); it
 * is what populates the catalog where Flyway is disabled (e.g. the H2 test profile).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankCatalogSeeder implements ApplicationRunner {

    private static final Map<String, String> CATALOG = Map.ofEntries(
            Map.entry("007", "GALICIA"),
            Map.entry("011", "NACION"),
            Map.entry("015", "ICBC"),
            Map.entry("016", "CITIBANK"),
            Map.entry("017", "BBVA"),
            Map.entry("027", "SUPERVIELLE"),
            Map.entry("034", "PATAGONIA"),
            Map.entry("044", "HIPOTECARIO"),
            Map.entry("072", "SANTANDER"),
            Map.entry("083", "BANCO_DEL_CHUBUT"),
            Map.entry("150", "HSBC"),
            Map.entry("285", "MACRO"),
            Map.entry("299", "BANCO_COMAFI"));

    private final BankJpaRepository bankJpaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int created = 0;
        for (Map.Entry<String, String> bank : CATALOG.entrySet()) {
            if (bankJpaRepository.findByBankNumber(bank.getKey()).isEmpty()) {
                bankJpaRepository.save(BankJpaEntity.builder()
                        .bankNumber(bank.getKey())
                        .name(bank.getValue())
                        .build());
                created++;
            }
        }
        if (created > 0) {
            log.info("Bank catalog seeded: {} new bank(s)", created);
        }
    }
}
