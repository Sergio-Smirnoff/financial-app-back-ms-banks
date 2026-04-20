package com.financialapp.banks.mapper;

import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.dto.response.BankResponse;
import com.financialapp.banks.model.entity.Bank;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface BankMapper {

    default BankResponse toResponse(Bank bank, List<AccountResponse> accounts, 
                                   Map<String, BigDecimal> totalBalances, 
                                   int cardsCount, int loansCount) {
        if (bank == null) return null;
        return BankResponse.builder()
                .id(bank.getId())
                .userId(bank.getUserId())
                .name(bank.getName())
                .logoUrl(bank.getLogoUrl())
                .accounts(accounts == null ? List.of() : accounts)
                .totalBalances(totalBalances)
                .accountsCount(accounts == null ? 0 : accounts.size())
                .cardsCount(cardsCount)
                .loansCount(loansCount)
                .createdAt(bank.getCreatedAt())
                .updatedAt(bank.getUpdatedAt())
                .build();
    }
}
