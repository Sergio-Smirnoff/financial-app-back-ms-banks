package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;

import java.util.List;
import java.util.Optional;

public interface BankRepository {
    List<Bank> findAll();
    Optional<Bank> findByName(BankName name);
    boolean existsByName(BankName name);
    Bank save(Bank bank);
    void delete(BankName name);
}
