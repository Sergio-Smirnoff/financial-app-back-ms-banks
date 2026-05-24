package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.domain.repository.LoanInstallmentRepository;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.LoanInstallmentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoanInstallmentRepositoryImpl implements LoanInstallmentRepository {

    private final LoanInstallmentJpaRepository jpaRepository;
    private final LoanJpaRepository loanJpaRepository;
    private final LoanInstallmentPersistenceMapper mapper;

    @Override
    public List<LoanInstallment> findByParentId(LoanId loanId) {
        return findByLoanId(loanId);
    }

    @Override
    public List<LoanInstallment> findByLoanId(LoanId loanId) {
        return jpaRepository.findByLoan_IdOrderByInstallmentNumberAsc(loanId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<LoanInstallment> findById(LoanInstallmentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByParentIdAndUnpaid(LoanId loanId) {
        return jpaRepository.findByLoan_IdOrderByInstallmentNumberAsc(loanId.value())
                .stream().anyMatch(li -> !li.isPaid());
    }

    @Override
    public boolean existsByParentIdAndDescriptionAndAmountAndDueDate(LoanId loanId, String description, Money amount, LocalDate dueDate) {
        return jpaRepository.findByLoan_IdOrderByInstallmentNumberAsc(loanId.value()).stream().anyMatch(li ->
                li.getAmount().compareTo(amount.amount()) == 0 && li.getDueDate().equals(dueDate));
    }

    @Override
    public List<LoanInstallment> findUpcomingUnpaid(LocalDate from, LocalDate to) {
        return jpaRepository.findUpcomingUnpaid(from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<LoanInstallment> findUpcomingUnpaid(UserId userId, LocalDate from, LocalDate to) {
        return jpaRepository.findUpcomingUnpaidByUser(userId.value(), from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public LoanInstallment save(LoanInstallment installment) {
        LoanJpaEntity loan = loanJpaRepository.findById(installment.loanId().value())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", installment.loanId().value().toString()));
        var entity = installment.id() != null
                ? jpaRepository.findById(installment.id().value())
                        .map(existing -> mapper.merge(existing, installment, loan))
                        .orElseGet(() -> mapper.toJpa(installment, loan))
                : mapper.toJpa(installment, loan);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public List<LoanInstallment> saveAll(List<LoanInstallment> installments) {
        return installments.stream().map(this::save).toList();
    }
}
