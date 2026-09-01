package com.financialapp.banks.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshotId;
import com.financialapp.banks.infrastructure.persistence.entity.BalanceSnapshotJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BalanceSnapshotPersistenceMapper {

    private final ObjectMapper objectMapper;

    public BalanceSnapshot toDomain(BalanceSnapshotJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BalanceSnapshot.reconstitute(
                new BalanceSnapshotId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getSnapshotDate(),
                deserializeMoneyList(entity.getCashByCurrency()),
                deserializeMoneyList(entity.getCardDebtByCurrency()),
                deserializeMoneyList(entity.getLoanDebtByCurrency()),
                entity.getCreatedAt()
        );
    }

    public BalanceSnapshotJpaEntity toJpa(BalanceSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return BalanceSnapshotJpaEntity.builder()
                .id(snapshot.id() != null ? snapshot.id().value() : null)
                .userId(snapshot.userId().value())
                .snapshotDate(snapshot.snapshotDate())
                .cashByCurrency(serializeMoneyList(snapshot.cashByCurrency()))
                .cardDebtByCurrency(serializeMoneyList(snapshot.cardDebtByCurrency()))
                .loanDebtByCurrency(serializeMoneyList(snapshot.loanDebtByCurrency()))
                .createdAt(snapshot.createdAt())
                .build();
    }

    public void merge(BalanceSnapshotJpaEntity existing, BalanceSnapshot snapshot) {
        existing.setUserId(snapshot.userId().value());
        existing.setSnapshotDate(snapshot.snapshotDate());
        existing.setCashByCurrency(serializeMoneyList(snapshot.cashByCurrency()));
        existing.setCardDebtByCurrency(serializeMoneyList(snapshot.cardDebtByCurrency()));
        existing.setLoanDebtByCurrency(serializeMoneyList(snapshot.loanDebtByCurrency()));
        existing.setCreatedAt(snapshot.createdAt());
    }

    String serializeMoneyList(List<Money> list) {
        if (list == null || list.isEmpty()) {
            return "{}";
        }
        Map<String, BigDecimal> map = list.stream()
                .collect(Collectors.toMap(
                        m -> m.currency().getCurrencyCode(),
                        Money::amount,
                        BigDecimal::add,
                        LinkedHashMap::new));
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize money list to JSON", ex);
        }
    }

    List<Money> deserializeMoneyList(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return List.of();
        }
        Map<String, BigDecimal> raw;
        try {
            raw = objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize money list from JSON", ex);
        }
        return raw.entrySet().stream()
                .map(e -> new Money(e.getValue(), Currency.getInstance(e.getKey())))
                .sorted(Comparator.comparing(m -> m.currency().getCurrencyCode()))
                .toList();
    }
}
