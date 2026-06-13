package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.infrastructure.persistence.entity.AccountJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class AccountPersistenceMapper {

    public Account toDomain(AccountJpaEntity entity) {
        if (entity == null) return null;
        Money balance = new Money(entity.getBalance(), Currency.getInstance(entity.getCurrency()));
        UserId userId = new UserId(entity.getUserId());
        BankNumber bankNumber = new BankNumber(entity.getBank().getBankNumber());
        Cbu cbu = Cbu.from(entity.getCbu());
        AccountType type = AccountType.valueOf(entity.getType());
        return new Account(type, cbu, entity.getAlias(), balance, userId, bankNumber,
                entity.getName(), entity.getIsActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public AccountJpaEntity toJpa(Account account, BankJpaEntity bank) {
        if (account == null) return null;
        return AccountJpaEntity.builder()
                .cbu(account.cbu().value())
                .alias(account.alias())
                .bank(bank)
                .userId(account.userId().value())
                .name(account.name())
                .type(account.type().name())
                .balance(account.balance().amount())
                .currency(account.balance().currency().getCurrencyCode())
                .isActive(account.isActive())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .build();
    }

    public AccountJpaEntity merge(AccountJpaEntity existing, Account account, BankJpaEntity bank) {
        existing.setCbu(account.cbu().value());
        existing.setAlias(account.alias());
        existing.setBank(bank);
        existing.setUserId(account.userId().value());
        existing.setName(account.name());
        existing.setType(account.type().name());
        existing.setBalance(account.balance().amount());
        existing.setCurrency(account.balance().currency().getCurrencyCode());
        existing.setIsActive(account.isActive());
        existing.setUpdatedAt(account.updatedAt());
        return existing;
    }
}
