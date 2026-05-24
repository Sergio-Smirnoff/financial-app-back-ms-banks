package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.web.dto.response.AvailableBankResponse;

import java.util.List;

public interface ListAvailableBanksUseCase {
    List<AvailableBankResponse> execute();
}
