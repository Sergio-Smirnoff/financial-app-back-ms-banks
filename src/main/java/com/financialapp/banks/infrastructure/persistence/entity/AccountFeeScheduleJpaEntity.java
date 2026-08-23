package com.financialapp.banks.infrastructure.persistence.entity;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "account_fee_schedules", schema = "banks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountFeeScheduleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_cbu", nullable = false, length = 22, unique = true)
    private String accountCbu;

    @Column(name = "maintenance_fee")
    private BigDecimal maintenanceFee;

    @Column(name = "transfer_fee")
    private BigDecimal transferFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "iva_treatment", nullable = false, length = 10)
    private IvaTreatment ivaTreatment;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}
