package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.usecase.ListAvailableBanksUseCase;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ListAvailableBanksUseCaseImpl implements ListAvailableBanksUseCase {

    @Override
    public List<AvailableBankResponse> execute() {
        return Arrays.stream(BankName.values())
                .map(b -> new AvailableBankResponse(b.name(), b.getDisplayName(), b.getLogoUrl()))
                .toList();
    }
}
