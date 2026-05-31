package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;

import java.util.List;
import java.util.Optional;

public interface BankRepository {
    Optional<Bank> findByBankNumber(BankNumber bankNumber);
    boolean existsByBankNumber(BankNumber bankNumber);
    List<Bank> findAll();
}
