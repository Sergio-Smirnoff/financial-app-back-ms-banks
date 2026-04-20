package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.mapper.BankMapper;
import com.financialapp.banks.model.dto.request.BankRequest;
import com.financialapp.banks.model.dto.response.BankResponse;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import com.financialapp.banks.repository.CardRepository;
import com.financialapp.banks.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock BankRepository bankRepository;
    @Mock AccountRepository accountRepository;
    @Mock CardRepository cardRepository;
    @Mock LoanRepository loanRepository;
    @Mock AccountService accountService;

    BankMapper bankMapper = new BankMapper() {};

    BankService service;

    @BeforeEach
    void setUp() {
        service = new BankService(bankRepository, accountRepository, cardRepository, loanRepository, bankMapper, accountService);
    }

    @Test
    void create_persistsBankForCurrentUser() {
        when(bankRepository.existsByUserIdAndName(1L, "Chase")).thenReturn(false);
        when(bankRepository.save(any(Bank.class))).thenAnswer(inv -> {
            Bank b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BankResponse res = service.create(1L, new BankRequest("Chase", null));

        assertThat(res.id()).isEqualTo(10L);
        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.name()).isEqualTo("Chase");
        assertThat(res.accounts()).isEmpty();
    }

    @Test
    void create_rejectsDuplicateName() {
        when(bankRepository.existsByUserIdAndName(1L, "Chase")).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, new BankRequest("Chase", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void get_returnsBankWithAccounts() {
        Bank bank = Bank.builder().id(10L).userId(1L).name("Chase").build();
        when(bankRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(bank));
        when(accountRepository.findByBankIdOrderByNameAsc(10L)).thenReturn(List.of());

        BankResponse res = service.get(10L, 1L);

        assertThat(res.id()).isEqualTo(10L);
        verify(accountRepository).findByBankIdOrderByNameAsc(10L);
    }

    @Test
    void get_missingBank_throws() {
        when(bankRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesBankAndCascades() {
        Bank bank = Bank.builder().id(10L).userId(1L).name("Chase").build();
        when(bankRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(bank));

        service.delete(10L, 1L);

        verify(bankRepository).delete(bank);
    }
}
