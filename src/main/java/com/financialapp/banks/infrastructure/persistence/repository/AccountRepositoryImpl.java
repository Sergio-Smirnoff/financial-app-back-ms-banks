package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.BankNumber;
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
    public List<Account> findByBankNumber(BankNumber bankNumber) {
        BankJpaEntity bank = requireBank(bankNumber);
        return accountJpaRepository.findByBank_IdOrderByNameAsc(bank.getId())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public int countByBankNumber(BankNumber bankNumber) {
        return accountJpaRepository.countByBank_BankNumber(bankNumber.value());
    }

    @Override
    public Optional<Account> findByCbu(String cbu) {
        return accountJpaRepository.findByCbu(cbu).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAliasAndBankNumber(String alias, BankNumber bankNumber) {
        return accountJpaRepository.findByAliasAndBank_BankNumber(alias, bankNumber.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndBankNumberAndName(UserId userId, BankNumber bankNumber, String name) {
        return accountJpaRepository.existsByUserIdAndBank_BankNumberAndName(userId.value(), bankNumber.value(), name);
    }

    @Override
    public boolean existsByUserIdAndBankNumberAndTypeAndCurrency(UserId userId, BankNumber bankNumber, String type, Currency currency) {
        return accountJpaRepository.existsByUserIdAndBank_BankNumberAndTypeAndCurrency(
                userId.value(), bankNumber.value(), type, currency.getCurrencyCode());
    }

    @Override
    public List<Account> findLowBalance(BigDecimal threshold) {
        return accountJpaRepository.findLowBalanceAccounts(threshold)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Account> findFiltered(UserId userId, String type, Currency currency,
                                      BankNumber bankNumber, String name, boolean hideEmpty) {
        return accountJpaRepository.findFiltered(
                        userId.value(),
                        type,
                        currency != null ? currency.getCurrencyCode() : null,
                        bankNumber != null ? bankNumber.value() : null,
                        name,
                        hideEmpty)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Account save(Account account) {
        BankJpaEntity bank = requireBank(account.bankNumber());

        AccountJpaEntity entity = accountJpaRepository.findByCbu(account.cbu().value())
                .map(existing -> mapper.merge(existing, account, bank))
                .orElseGet(() -> mapper.toJpa(account, bank));

        return mapper.toDomain(accountJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(String cbu) {
        accountJpaRepository.deleteByCbu(cbu);
    }

    private BankJpaEntity requireBank(BankNumber bankNumber) {
        return bankJpaRepository.findByBankNumber(bankNumber.value())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", bankNumber.value()));
    }
}
