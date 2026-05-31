package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.impl.ListAvailableBanksUseCaseImpl;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAvailableBanksUseCaseImplTest {

    @Mock BankRepository bankRepository;

    @Test
    void execute_returnsTheCatalogFromTheRepository() {
        List<Bank> catalog = List.of(
                new Bank(new BankNumber("007"), "GALICIA", null),
                new Bank(new BankNumber("072"), "SANTANDER", null));
        when(bankRepository.findAll()).thenReturn(catalog);

        ListAvailableBanksUseCaseImpl useCase = new ListAvailableBanksUseCaseImpl(bankRepository);

        assertThat(useCase.execute()).isEqualTo(catalog);
    }
}
