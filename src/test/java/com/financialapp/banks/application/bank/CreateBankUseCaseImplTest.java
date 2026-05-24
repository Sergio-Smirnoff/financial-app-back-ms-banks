package com.financialapp.banks.application.bank;

import com.financialapp.banks.application.bank.command.CreateBankCommand;
import com.financialapp.banks.application.bank.impl.CreateBankUseCaseImpl;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.domain.repository.BankRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBankUseCaseImplTest {

    @Mock BankRepository bankRepository;
    CreateBankUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBankUseCaseImpl(bankRepository);
    }

    @Test
    void create_persistsNewBank() {
        when(bankRepository.existsByName(BankName.GALICIA)).thenReturn(false);
        when(bankRepository.save(any(Bank.class))).thenAnswer(inv -> inv.getArgument(0));

        Bank result = useCase.execute(new CreateBankCommand(BankName.GALICIA, new Logo("http://logo")));

        assertThat(result.name()).isEqualTo(BankName.GALICIA);
        assertThat(result.logo().url()).isEqualTo("http://logo");
    }

    @Test
    void create_rejectsDuplicateName() {
        when(bankRepository.existsByName(BankName.GALICIA)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CreateBankCommand(BankName.GALICIA, null)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }
}
