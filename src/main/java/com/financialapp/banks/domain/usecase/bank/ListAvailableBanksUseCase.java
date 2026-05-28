package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.web.dto.response.AvailableBankResponse;

import java.util.List;

public interface ListAvailableBanksUseCase {
    List<AvailableBankResponse> execute();
}
