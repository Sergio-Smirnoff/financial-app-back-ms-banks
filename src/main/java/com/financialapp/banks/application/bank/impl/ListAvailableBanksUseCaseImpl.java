package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.domain.usecase.bank.ListAvailableBanksUseCase;
import com.financialapp.banks.domain.model.bank.BankName;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ListAvailableBanksUseCaseImpl implements ListAvailableBanksUseCase {

    @Override
    public List<BankName> execute() {
        return Arrays.stream(BankName.values()).toList();
    }
}
