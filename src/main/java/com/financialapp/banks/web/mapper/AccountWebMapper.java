package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.web.dto.response.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .id(account.id().value())
                .bankName(account.bankName().name())
                .userId(account.userId().value())
                .name(account.details().name())
                .type(account.details().type())
                .balance(account.details().balance().amount())
                .currency(account.details().balance().currency().getCurrencyCode())
                .cbu(account.information().cbu())
                .alias(account.information().alias())
                .isActive(account.details().isActive())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .build();
    }
}
