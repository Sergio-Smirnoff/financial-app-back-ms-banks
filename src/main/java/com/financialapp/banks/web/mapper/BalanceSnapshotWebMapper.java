package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.web.dto.response.BalanceSnapshotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BalanceSnapshotWebMapper {

    public BalanceSnapshotResponse toResponse(BalanceSnapshot snapshot) {
        if (snapshot == null) return null;

        return BalanceSnapshotResponse.builder()
                .snapshotDate(snapshot.snapshotDate())
                .cashByCurrency(toMap(snapshot.cashByCurrency()))
                .cardDebtByCurrency(toMap(snapshot.cardDebtByCurrency()))
                .loanDebtByCurrency(toMap(snapshot.loanDebtByCurrency()))
                .build();
    }

    private Map<String, String> toMap(List<Money> monies) {
        if (monies == null) return Map.of();
        return monies.stream().collect(Collectors.toMap(
                m -> m.currency().getCurrencyCode(),
                m -> m.amount().toPlainString()
        ));
    }
}
