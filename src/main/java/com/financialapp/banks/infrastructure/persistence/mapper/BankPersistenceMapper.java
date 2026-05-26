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
        BankName name = BankName.valueOf(entity.getName());
        return new Bank(name, new Logo(name.getLogoUrl()));
    }
}
