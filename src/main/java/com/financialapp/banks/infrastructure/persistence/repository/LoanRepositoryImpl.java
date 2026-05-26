package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BankJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.LoanJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.LoanPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoanRepositoryImpl implements LoanRepository {

    private final LoanJpaRepository loanJpaRepository;
    private final BankJpaRepository bankJpaRepository;
    private final LoanPersistenceMapper mapper;

    @Override
    public List<Loan> findByUserId(UserId userId) {
        return loanJpaRepository.findByUserId(userId.value())
                .stream().map(this::loadDomain).toList();
    }

    @Override
    public List<Loan> findByBankName(BankName bankName) {
        BankJpaEntity bank = requireBank(bankName);
        return loanJpaRepository.findByBankId(bank.getId())
                .stream().map(entity -> mapper.toDomain(entity, bank)).toList();
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        return loanJpaRepository.findById(id.value()).map(this::loadDomain);
    }

    @Override
    public Optional<Loan> findByIdAndUserId(LoanId id, UserId userId) {
        return loanJpaRepository.findByIdAndUserId(id.value(), userId.value()).map(this::loadDomain);
    }

    @Override
    public int countByBankName(BankName bankName) {
        return bankJpaRepository.findByName(bankName.name())
                .map(bank -> loanJpaRepository.countByBankId(bank.getId())).orElse(0);
    }

    @Override
    public List<Loan> findActiveWithUpcomingPayment(LocalDate from, LocalDate to) {
        return loanJpaRepository.findActiveWithUpcomingPayment(from, to)
                .stream().map(this::loadDomain).toList();
    }

    @Override
    @Transactional
    public Loan save(Loan loan) {
        BankJpaEntity bank = requireBank(loan.bankName());
        LoanJpaEntity entity = loan.id() != null && loan.id().value() != null
                ? loanJpaRepository.findById(loan.id().value())
                        .map(existing -> mapper.merge(existing, loan, bank))
                        .orElseGet(() -> mapper.toJpa(loan, bank))
                : mapper.toJpa(loan, bank);
        return mapper.toDomain(loanJpaRepository.save(entity), bank);
    }

    @Override
    @Transactional
    public void delete(LoanId id) {
        loanJpaRepository.deleteById(id.value());
    }

    private Loan loadDomain(LoanJpaEntity entity) {
        BankJpaEntity bank = bankJpaRepository.findById(entity.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", entity.getId().toString()));
        return mapper.toDomain(entity, bank);
    }

    private BankJpaEntity requireBank(BankName name) {
        return bankJpaRepository.findByName(name.name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank", name.getDisplayName()));
    }
}
