package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.web.dto.response.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {

    public AccountResponse toResponse(Account account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .bankNumber(account.bankNumber().value())
                .userId(account.userId().value())
                .name(account.name())
                .type(account.type().name())
                .balance(account.balance().amount().toPlainString())
                .currency(account.balance().currency().getCurrencyCode())
                .cbu(account.cbu().value())
                .alias(account.alias())
                .isActive(account.isActive())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .build();
    }
}
