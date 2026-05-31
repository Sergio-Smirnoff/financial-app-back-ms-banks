package com.financialapp.banks.application.account;

import com.financialapp.banks.domain.usecase.account.command.CloseAccountCommand;
import com.financialapp.banks.application.account.impl.CloseAccountUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.exception.InvestmentsServiceException;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
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
class CloseAccountUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    @Mock InvestmentsPort investmentsPort;
    CloseAccountUseCaseImpl useCase;

    private static final Cbu CBU = Cbu.from("0070001600000000123459");
    private static final BankNumber BANK_NUMBER = new BankNumber("007");

    @BeforeEach
    void setUp() {
        useCase = new CloseAccountUseCaseImpl(accountRepository, investmentsPort);
    }

    private InvestmentAccount investmentAccount() {
        return new InvestmentAccount(
                CBU, "alias.test",
                new Money(BigDecimal.ZERO, Currency.getInstance("ARS")),
                new UserId(1L), BANK_NUMBER, "My Investment Account",
                true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_wrapsInfrastructureExceptionAsInvestmentsServiceException() {
        when(accountRepository.findByCbu(CBU.value()))
                .thenReturn(Optional.of(investmentAccount()));
        when(investmentsPort.countHoldings(CBU.value()))
                .thenThrow(new InfrastructureException("ms-investments: timeout"));

        assertThatThrownBy(() -> useCase.execute(new CloseAccountCommand(CBU.value())))
                .isInstanceOf(InvestmentsServiceException.class)
                .isNotInstanceOf(InfrastructureException.class);
    }

    @Test
    void execute_deletesAccountWhenNoHoldings() {
        when(accountRepository.findByCbu(CBU.value()))
                .thenReturn(Optional.of(investmentAccount()));
        when(investmentsPort.countHoldings(CBU.value())).thenReturn(0);

        useCase.execute(new CloseAccountCommand(CBU.value()));

        verify(accountRepository).delete(CBU.value());
    }
}
