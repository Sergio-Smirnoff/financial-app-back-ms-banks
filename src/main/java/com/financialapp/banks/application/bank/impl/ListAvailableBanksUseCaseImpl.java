package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.domain.usecase.bank.ListAvailableBanksUseCase;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAvailableBanksUseCaseImpl implements ListAvailableBanksUseCase {

    private final BankRepository bankRepository;

    @Override
    public List<Bank> execute() {
        return bankRepository.findAll();
    }
}
