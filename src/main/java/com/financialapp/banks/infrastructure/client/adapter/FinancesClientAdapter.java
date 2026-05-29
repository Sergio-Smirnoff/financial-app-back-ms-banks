package com.financialapp.banks.infrastructure.client.adapter;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.port.FinancesPort;
import com.financialapp.banks.infrastructure.client.FinancesFeignClient;
import com.financialapp.banks.domain.exception.InfrastructureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancesClientAdapter implements FinancesPort {

    private final FinancesFeignClient client;

    @Override
    public List<TransactionSummary> getRecentTransactions(String accountCbu, int limit) {
        return fetchAndMap(accountCbu, limit, null, null);
    }

    @Override
    public List<TransactionSummary> getAllTransactions(String accountCbu) {
        return fetchAndMap(accountCbu, null, null, null);
    }

    @Override
    public List<TransactionSummary> getFilteredTransactions(String accountCbu, LocalDate from, LocalDate to) {
        return fetchAndMap(accountCbu, null, from, to);
    }

    private List<TransactionSummary> fetchAndMap(String accountCbu, Integer limit, LocalDate from, LocalDate to) {
        try {
            var response = client.getTransactions(accountCbu, limit, from, to);
            if (response == null || response.data() == null) return List.of();
            return response.data().stream()
                    .map(dto -> new TransactionSummary(
                            dto.transactionId(),
                            dto.accountCbu(),
                            new Money(dto.amount(), Currency.getInstance(dto.currency())),
                            dto.description(),
                            dto.category(),
                            dto.subcategory(),
                            dto.date()))
                    .toList();
        } catch (Exception e) {
            log.error("ms-finances call failed [fetchTransactions] for accountCbu={}: {}", accountCbu, e.getMessage(), e);
            throw new InfrastructureException("ms-finances: " + e.getMessage());
        }
    }
}
