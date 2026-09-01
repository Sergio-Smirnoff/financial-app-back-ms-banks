package com.financialapp.banks.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_snapshots", schema = "banks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "snapshot_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSnapshotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "cash_by_currency", nullable = false, columnDefinition = "JSONB")
    private String cashByCurrency;

    @Column(name = "card_debt_by_currency", nullable = false, columnDefinition = "JSONB")
    private String cardDebtByCurrency;

    @Column(name = "loan_debt_by_currency", nullable = false, columnDefinition = "JSONB")
    private String loanDebtByCurrency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
