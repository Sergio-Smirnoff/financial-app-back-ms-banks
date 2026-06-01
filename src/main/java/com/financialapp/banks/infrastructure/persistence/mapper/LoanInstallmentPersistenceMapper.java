package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.infrastructure.persistence.entity.LoanInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class LoanInstallmentPersistenceMapper {

    public LoanInstallment toDomain(LoanInstallmentJpaEntity entity) {
        if (entity == null) return null;
        LoanJpaEntity loan = entity.getLoan();
        Currency currency = Currency.getInstance(loan.getCurrency());
        return new LoanInstallment(
                new LoanInstallmentId(entity.getId()),
                new LoanId(loan.getId()),
                entity.getInstallmentNumber(),
                new Money(entity.getAmount(), currency),
                entity.getDueDate(),
                entity.isPaid(),
                entity.getPaidDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public LoanInstallmentJpaEntity toJpa(LoanInstallment installment, LoanJpaEntity loan) {
        if (installment == null) return null;
        return LoanInstallmentJpaEntity.builder()
                .id(installment.id() != null ? installment.id().value() : null)
                .loan(loan)
                .installmentNumber(installment.installmentNumber())
                .amount(installment.amount().amount())
                .dueDate(installment.dueDate())
                .paid(installment.paid())
                .paidDate(installment.paidDate())
                .createdAt(installment.createdAt())
                .updatedAt(installment.updatedAt())
                .build();
    }

    public LoanInstallmentJpaEntity merge(LoanInstallmentJpaEntity existing, LoanInstallment installment, LoanJpaEntity loan) {
        existing.setLoan(loan);
        existing.setInstallmentNumber(installment.installmentNumber());
        existing.setAmount(installment.amount().amount());
        existing.setDueDate(installment.dueDate());
        existing.setPaid(installment.paid());
        existing.setPaidDate(installment.paidDate());
        existing.setUpdatedAt(installment.updatedAt());
        return existing;
    }
}
