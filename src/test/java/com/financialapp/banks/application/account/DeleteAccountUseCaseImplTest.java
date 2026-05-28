package com.financialapp.banks.application.account;

import com.financialapp.banks.domain.usecase.account.command.DeleteAccountCommand;
import com.financialapp.banks.application.account.impl.DeleteAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.exception.InvestmentsServiceException;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.port.InvestmentsPort;
import com.financialapp.banks.domain.repository.AccountRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    @Mock InvestmentsPort investmentsPort;
    DeleteAccountUseCaseImpl useCase;

    private static final String CBU = "0000003100012345678901";
    private static final BankName BANK_NAME = BankName.GALICIA;

    @BeforeEach
    void setUp() {
        useCase = new DeleteAccountUseCaseImpl(accountRepository, investmentsPort);
    }

    private InvestmentAccount investmentAccount() {
        return new InvestmentAccount(
                CBU, "alias.test",
                new Money(BigDecimal.ZERO, Currency.getInstance("ARS")),
                new UserId(1L), BANK_NAME, "My Investment Account",
                true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_wrapsInfrastructureExceptionAsInvestmentsServiceException() {
        when(accountRepository.findByCbu(CBU))
                .thenReturn(Optional.of(investmentAccount()));
        when(investmentsPort.countHoldings(CBU))
                .thenThrow(new InfrastructureException("ms-investments: timeout"));

        assertThatThrownBy(() -> useCase.execute(new DeleteAccountCommand(CBU)))
                .isInstanceOf(InvestmentsServiceException.class)
                .isNotInstanceOf(InfrastructureException.class);
    }

    @Test
    void execute_deletesAccountWhenNoHoldings() {
        when(accountRepository.findByCbu(CBU))
                .thenReturn(Optional.of(investmentAccount()));
        when(investmentsPort.countHoldings(CBU)).thenReturn(0);

        useCase.execute(new DeleteAccountCommand(CBU));

        verify(accountRepository).delete(CBU);
    }
}
