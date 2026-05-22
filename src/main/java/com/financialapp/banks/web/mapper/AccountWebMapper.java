package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.web.dto.response.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .bankName(account.bankName().name())
                .userId(account.userId().value())
                .name(account.name())
                .type(account.type())
                .balance(account.balance().amount())
                .currency(account.balance().currency().getCurrencyCode())
                .cbu(account.cbu())
                .alias(account.alias())
                .isActive(account.isActive())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .build();
    }
}
