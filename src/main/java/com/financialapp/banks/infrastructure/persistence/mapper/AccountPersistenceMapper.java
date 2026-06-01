package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.exception.InfrastructureException;
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
        return switch (type) {
            case CHECKING -> new CheckingAccount(
                    cbu, entity.getAlias(), balance, userId, bankNumber,
                    entity.getName(), entity.getIsActive(), entity.getCreatedAt(), entity.getUpdatedAt());
            case SAVINGS -> new SavingsAccount(
                    cbu, entity.getAlias(), balance, userId, bankNumber,
                    entity.getName(), entity.getIsActive(), entity.getCreatedAt(), entity.getUpdatedAt());
            case INVESTMENT -> new InvestmentAccount(
                    cbu, entity.getAlias(), balance, userId, bankNumber,
                    entity.getName(), entity.getIsActive(), entity.getCreatedAt(), entity.getUpdatedAt());
        };
    }

    public AccountJpaEntity toJpa(Account account, BankJpaEntity bank) {
        if (account == null) return null;
        return AccountJpaEntity.builder()
                .cbu(account.cbu().value())
                .alias(account.alias())
                .bank(bank)
                .userId(account.userId().value())
                .name(account.name())
                .type(resolveType(account))
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
        existing.setType(resolveType(account));
        existing.setBalance(account.balance().amount());
        existing.setCurrency(account.balance().currency().getCurrencyCode());
        existing.setIsActive(account.isActive());
        existing.setUpdatedAt(account.updatedAt());
        return existing;
    }

    private String resolveType(Account account) {
        if (account instanceof CheckingAccount) return AccountType.CHECKING.name();
        if (account instanceof SavingsAccount) return AccountType.SAVINGS.name();
        if (account instanceof InvestmentAccount) return AccountType.INVESTMENT.name();
        throw new InfrastructureException("Unknown account subtype: " + account.getClass().getSimpleName());
    }
}
