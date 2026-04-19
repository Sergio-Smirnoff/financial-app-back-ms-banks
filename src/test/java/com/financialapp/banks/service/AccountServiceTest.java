package com.financialapp.banks.service;

import com.financialapp.banks.client.InvestmentsClient;
import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.mapper.AccountMapper;
import com.financialapp.banks.model.dto.request.AccountRequest;
import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.enums.AccountType;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock BankRepository bankRepository;
    @Mock AccountRepository accountRepository;
    @Mock InvestmentsClient investmentsClient;

    AccountMapper accountMapper = new AccountMapper() {};

    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, bankRepository, accountMapper, investmentsClient);
    }

    @Test
    void create_persistsAccountUnderOwnedBank() {
        Bank bank = Bank.builder().id(10L).userId(1L).name("Chase").build();
        when(bankRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(bank));
        when(accountRepository.existsByBankIdAndName(10L, "Savings")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(42L);
            return a;
        });

        AccountRequest req = new AccountRequest(10L, "Savings", AccountType.SAVINGS,
                new BigDecimal("100.00"), "USD", true);
        AccountResponse res = service.create(1L, req);

        assertThat(res.id()).isEqualTo(42L);
        assertThat(res.bankId()).isEqualTo(10L);
        assertThat(res.type()).isEqualTo(AccountType.SAVINGS);
        assertThat(res.currency()).isEqualTo("USD");
    }

    @Test
    void create_rejectsAccountOnBankOwnedByAnotherUser() {
        when(bankRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        AccountRequest req = new AccountRequest(10L, "Savings", AccountType.SAVINGS,
                BigDecimal.ZERO, "USD", true);
        assertThatThrownBy(() -> service.create(2L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bank not found");
    }

    @Test
    void create_rejectsDuplicateAccountName() {
        Bank bank = Bank.builder().id(10L).userId(1L).name("Chase").build();
        when(bankRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(bank));
        when(accountRepository.existsByBankIdAndName(10L, "Savings")).thenReturn(true);

        AccountRequest req = new AccountRequest(10L, "Savings", AccountType.SAVINGS,
                BigDecimal.ZERO, "USD", true);
        assertThatThrownBy(() -> service.create(1L, req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void adjustBalance_updatesBalanceCorrectly() {
        Account account = Account.builder()
                .id(1L)
                .balance(new BigDecimal("100.00"))
                .build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        service.adjustBalance(1L, new BigDecimal("50.50"));

        assertThat(account.getBalance()).isEqualByComparingTo("150.50");
    }

    @Test
    void adjustBalance_throwsExceptionWhenAccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adjustBalance(1L, new BigDecimal("50.50")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }
}
