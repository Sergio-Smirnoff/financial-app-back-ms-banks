package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.usecase.ListBanksUseCase;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListBanksUseCaseImpl implements ListBanksUseCase {

    private final BankRepository bankRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Bank> execute() {
        return bankRepository.findAll();
    }
}
