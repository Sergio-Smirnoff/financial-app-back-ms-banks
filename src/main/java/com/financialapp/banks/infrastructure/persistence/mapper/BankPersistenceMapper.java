package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BankPersistenceMapper {

    public Bank toDomain(BankJpaEntity entity) {
        if (entity == null) return null;
        return new Bank(BankName.valueOf(entity.getName()), new Logo(entity.getLogoUrl()));
    }

    public BankJpaEntity toJpa(Bank bank) {
        if (bank == null) return null;
        return BankJpaEntity.builder()
                .name(bank.name().name())
                .logoUrl(bank.logo() != null ? bank.logo().url() : null)
                .build();
    }

    public BankJpaEntity merge(BankJpaEntity existing, Bank bank) {
        existing.setName(bank.name().name());
        existing.setLogoUrl(bank.logo() != null ? bank.logo().url() : null);
        return existing;
    }
}
