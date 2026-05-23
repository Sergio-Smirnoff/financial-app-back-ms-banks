package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
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
                .type(resolveType(account))
                .balance(account.balance().amount())
                .currency(account.balance().currency().getCurrencyCode())
                .cbu(account.cbu())
                .alias(account.alias())
                .isActive(account.isActive())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .build();
    }

    private String resolveType(Account account) {
        if (account instanceof CheckingAccount) return AccountType.CHECKING.name();
        if (account instanceof SavingsAccount) return AccountType.SAVINGS.name();
        if (account instanceof InvestmentAccount) return AccountType.INVESTMENT.name();
        throw new IllegalStateException("Unknown account subtype: " + account.getClass().getSimpleName());
    }
}
