package com.financialapp.banks.application.account;

import com.financialapp.banks.application.account.impl.CloseAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceConflictException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.port.InvestmentsPort;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.usecase.account.command.CloseAccountCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Branch coverage for CloseAccount: not-found, holdings>0 conflict, non-investment zero/non-zero balance. */
@ExtendWith(MockitoExtension.class)
class CloseAccountBranchesTest {

    @Mock AccountRepository accountRepository;
    @Mock InvestmentsPort investmentsPort;
    CloseAccountUseCaseImpl useCase;

    private static final Cbu CBU = Cbu.from("0070001600000000123459");
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");

    @BeforeEach
    void setUp() {
        useCase = new CloseAccountUseCaseImpl(accountRepository, investmentsPort);
    }

    private CheckingAccount checking(String balance) {
        return new CheckingAccount(CBU, "alias", new Money(new BigDecimal(balance), ARS),
                new UserId(1L), BANK, "acc", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private InvestmentAccount investment() {
        return new InvestmentAccount(CBU, "alias", new Money(BigDecimal.ZERO, ARS),
                new UserId(1L), BANK, "inv", true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void throwsNotFound_whenAccountMissing() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(new CloseAccountCommand(CBU.value())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(accountRepository, never()).delete(CBU.value());
    }

    @Test
    void rejectsInvestmentAccount_whenHoldingsRemain() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(investment()));
        when(investmentsPort.countHoldings(CBU.value())).thenReturn(3);

        assertThatThrownBy(() -> useCase.execute(new CloseAccountCommand(CBU.value())))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("active holdings");
        verify(accountRepository, never()).delete(CBU.value());
    }

    @Test
    void rejectsNonInvestmentAccount_whenBalanceNonZero() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(checking("100.00")));

        assertThatThrownBy(() -> useCase.execute(new CloseAccountCommand(CBU.value())))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("non-zero balance");
        verify(accountRepository, never()).delete(CBU.value());
    }

    @Test
    void deletesNonInvestmentAccount_whenBalanceZero() {
        when(accountRepository.findByCbu(CBU.value())).thenReturn(Optional.of(checking("0.00")));

        useCase.execute(new CloseAccountCommand(CBU.value()));

        verify(accountRepository).delete(CBU.value());
    }
}
