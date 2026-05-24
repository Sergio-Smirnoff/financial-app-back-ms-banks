package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.infrastructure.persistence.entity.AccountJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.AccountJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.AccountPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final BankJpaRepository bankJpaRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public List<Account> findByUserId(UserId userId) {
        return accountJpaRepository.findByUserIdOrderByNameAsc(userId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Account> findByBankName(BankName bankName) {
        BankJpaEntity bank = bankJpaRepository.findByName(bankName.name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", bankName.getDisplayName()));
        return accountJpaRepository.findByBank_IdOrderByNameAsc(bank.getId())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public int countByBankName(BankName bankName) {
        return accountJpaRepository.countByBank_Name(bankName.name());
    }

    @Override
    public Optional<Account> findByCbu(String cbu) {
        return accountJpaRepository.findByCbu(cbu).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByCbuAndBankName(String cbu, BankName bankName) {
        return accountJpaRepository.findByCbuAndBank_Name(cbu, bankName.name()).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAliasAndBankName(String alias, BankName bankName) {
        return accountJpaRepository.findByAliasAndBank_Name(alias, bankName.name()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByBankNameAndName(BankName bankName, String name) {
        return accountJpaRepository.existsByBank_NameAndName(bankName.name(), name);
    }

    @Override
    public boolean existsByBankNameAndTypeAndCurrency(BankName bankName, String type, Currency currency) {
        return accountJpaRepository.existsByBank_NameAndTypeAndCurrency(
                bankName.name(), type, currency.getCurrencyCode());
    }

    @Override
    public List<Account> findLowBalance(BigDecimal threshold) {
        return accountJpaRepository.findLowBalanceAccounts(threshold)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Account> findFiltered(UserId userId, String type, Currency currency,
                                      BankName bankName, String name, boolean hideEmpty) {
        return accountJpaRepository.findFiltered(
                        userId.value(),
                        type,
                        currency != null ? currency.getCurrencyCode() : null,
                        bankName != null ? bankName.name() : null,
                        name,
                        hideEmpty)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Account save(Account account) {
        BankJpaEntity bank = bankJpaRepository.findByName(account.bankName().name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", account.bankName().getDisplayName()));

        AccountJpaEntity entity = accountJpaRepository.findByCbu(account.cbu())
                .map(existing -> mapper.merge(existing, account, bank))
                .orElseGet(() -> mapper.toJpa(account, bank));

        return mapper.toDomain(accountJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(String cbu) {
        accountJpaRepository.deleteByCbu(cbu);
    }
}
