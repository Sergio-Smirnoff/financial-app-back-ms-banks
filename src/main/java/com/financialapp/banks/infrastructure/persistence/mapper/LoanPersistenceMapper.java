package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Currency;
import java.util.List;

@Component
public class LoanPersistenceMapper {

    public Loan toDomain(LoanJpaEntity entity, BankJpaEntity bank) {
        if (entity == null) return null;
        Currency currency = Currency.getInstance(entity.getCurrency());
        Money principal = new Money(entity.getPrincipal(), currency);
        List<LoanInstallment> installments = entity.getInstallments().stream()
                .sorted(Comparator.comparingInt(LoanInstallmentJpaEntity::getInstallmentNumber))
                .map(child -> new LoanInstallment(
                        new LoanInstallmentId(child.getId()),
                        new LoanId(entity.getId()),
                        child.getInstallmentNumber(),
                        new Money(child.getAmount(), currency),
                        child.getDueDate(),
                        child.isPaid(),
                        child.getPaidDate(),
                        child.getCreatedAt(),
                        child.getUpdatedAt()))
                .toList();
        return new Loan(
                new LoanId(entity.getId()),
                new UserId(entity.getUserId()),
                new BankNumber(bank.getBankNumber()),
                entity.getName(),
                principal,
                entity.getInterestRate(),
                entity.getTotalInstallments(),
                entity.getRemainingInstallments(),
                AmortizationType.FRENCH,
                entity.getStartDate(),
                entity.isActive(),
                installments,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public LoanJpaEntity toJpa(Loan loan, BankJpaEntity bank) {
        if (loan == null) return null;
        LoanJpaEntity entity = LoanJpaEntity.builder()
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
        syncInstallments(entity, loan);
        return entity;
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
        syncInstallments(existing, loan);
        return existing;
    }

    private void syncInstallments(LoanJpaEntity loanEntity, Loan loan) {
        loanEntity.getInstallments().clear();
        for (LoanInstallment installment : loan.installments()) {
            LoanInstallmentJpaEntity child = LoanInstallmentJpaEntity.builder()
                    .id(installment.id() != null ? installment.id().value() : null)
                    .loan(loanEntity)
                    .installmentNumber(installment.installmentNumber())
                    .amount(installment.amount().amount())
                    .dueDate(installment.dueDate())
                    .paid(installment.paid())
                    .paidDate(installment.paidDate())
                    .createdAt(installment.createdAt())
                    .updatedAt(installment.updatedAt())
                    .build();
            loanEntity.getInstallments().add(child);
        }
    }
}
