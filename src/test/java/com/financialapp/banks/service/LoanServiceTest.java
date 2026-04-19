package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.mapper.LoanInstallmentMapper;
import com.financialapp.banks.mapper.LoanMapper;
import com.financialapp.banks.model.dto.request.LoanRequest;
import com.financialapp.banks.model.dto.response.LoanInstallmentResponse;
import com.financialapp.banks.model.dto.response.LoanResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Loan;
import com.financialapp.banks.model.entity.LoanInstallment;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.LoanInstallmentRepository;
import com.financialapp.banks.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock LoanInstallmentRepository installmentRepository;
    @Mock AccountRepository accountRepository;

    LoanMapper loanMapper = new LoanMapper() {};
    LoanInstallmentMapper installmentMapper = new LoanInstallmentMapper() {};

    LoanService service;

    @BeforeEach
    void setUp() {
        service = new LoanService(loanRepository, installmentRepository, accountRepository, loanMapper, installmentMapper);
    }

    @Test
    void create_generatesAmortizedInstallments() {
        Account account = Account.builder().id(100L).userId(1L).currency("USD").build();
        when(accountRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(account));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            l.setId(500L);
            return l;
        });

        LoanRequest request = new LoanRequest(100L, "Car Loan", new BigDecimal("1000.00"),
                new BigDecimal("10.00"), 3, LocalDate.of(2026, 1, 1));

        LoanResponse res = service.create(1L, request);

        assertThat(res.id()).isEqualTo(500L);
        assertThat(res.principal()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(res.totalInstallments()).isEqualTo(3);
        verify(installmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void payInstallment_marksAsPaid() {
        Loan loan = Loan.builder().id(500L).userId(1L).active(true).totalInstallments(3).remainingInstallments(3).build();
        LoanInstallment inst = LoanInstallment.builder().id(1000L).loan(loan).paid(false).build();

        when(loanRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findById(1000L)).thenReturn(Optional.of(inst));
        when(installmentRepository.save(inst)).thenReturn(inst);
        when(loanRepository.save(loan)).thenReturn(loan);

        LoanInstallmentResponse res = service.payInstallment(500L, 1000L, 1L, LocalDate.now());

        assertThat(res.paid()).isTrue();
        assertThat(loan.getRemainingInstallments()).isEqualTo(2);
    }
}
