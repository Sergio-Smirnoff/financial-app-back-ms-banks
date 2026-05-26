package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;

import java.util.Optional;

public interface BankRepository {
    Optional<Bank> findByName(BankName name);
}
