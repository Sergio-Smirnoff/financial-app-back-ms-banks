package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BankPersistenceMapper {

    public Bank toDomain(BankJpaEntity entity) {
        if (entity == null) return null;
        return new Bank(new BankNumber(entity.getBankNumber()), entity.getName(), new Logo(null));
    }
}
