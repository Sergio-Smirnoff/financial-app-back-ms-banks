package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class LoanPersistenceMapper {

    public Loan toDomain(LoanJpaEntity entity, BankJpaEntity bank) {
        if (entity == null) return null;
        Money principal = new Money(entity.getPrincipal(), Currency.getInstance(entity.getCurrency()));
        return new Loan(
                new LoanId(entity.getId()),
                new UserId(entity.getUserId()),
                BankName.valueOf(bank.getName()),
                entity.getName(),
                principal,
                entity.getInterestRate(),
                entity.getTotalInstallments(),
                entity.getRemainingInstallments(),
                AmortizationType.FRENCH,
                entity.getStartDate(),
                entity.isActive(),
                java.util.List.of(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public LoanJpaEntity toJpa(Loan loan, BankJpaEntity bank) {
        if (loan == null) return null;
        return LoanJpaEntity.builder()
                .id(loan.id() != null ? loan.id().value() : null)
                .bankId(bank.getId())
                .userId(loan.userId().value())
                .name(loan.name())
                .principal(loan.principal().amount())
                .currency(loan.principal().currency().getCurrencyCode())
                .interestRate(loan.interestRate())
                .totalInstallments(loan.totalInstallments())
                .remainingInstallments(loan.remainingInstallments())
                .startDate(loan.startDate())
                .active(loan.active())
                .createdAt(loan.createdAt())
                .updatedAt(loan.updatedAt())
                .build();
    }

    public LoanJpaEntity merge(LoanJpaEntity existing, Loan loan, BankJpaEntity bank) {
        existing.setBankId(bank.getId());
        existing.setUserId(loan.userId().value());
        existing.setName(loan.name());
        existing.setPrincipal(loan.principal().amount());
        existing.setCurrency(loan.principal().currency().getCurrencyCode());
        existing.setInterestRate(loan.interestRate());
        existing.setTotalInstallments(loan.totalInstallments());
        existing.setRemainingInstallments(loan.remainingInstallments());
        existing.setStartDate(loan.startDate());
        existing.setActive(loan.active());
        existing.setUpdatedAt(loan.updatedAt());
        return existing;
    }
}
