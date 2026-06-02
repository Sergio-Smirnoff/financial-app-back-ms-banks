package com.financialapp.banks.domain.gateway;

import com.financialapp.banks.domain.common.model.Money;

import java.time.LocalDate;
import java.util.List;

public interface FinancesGateway {

    record TransactionSummary(
            Long transactionId,
            String accountCbu,
            Money amount,
            String description,
            String category,
            String subcategory,
            LocalDate date
    ) {}

    List<TransactionSummary> getRecentTransactions(String accountCbu, int limit);
    List<TransactionSummary> getAllTransactions(String accountCbu);
    List<TransactionSummary> getFilteredTransactions(String accountCbu, LocalDate from, LocalDate to);
}
