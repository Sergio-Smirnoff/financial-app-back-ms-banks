package com.financialapp.banks.application.card;

import com.financialapp.banks.application.card.command.CreateCardCommand;
import com.financialapp.banks.application.card.impl.CreateCardUseCaseImpl;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.CreditCard;
import com.financialapp.banks.domain.model.card.cardPaymentMethod.DebitCard;
import com.financialapp.banks.domain.repository.BankRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCardUseCaseImplTest {

    @Mock CardRepository cardRepository;
    @Mock BankRepository bankRepository;
    CreateCardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCardUseCaseImpl(cardRepository, bankRepository);
    }

    private CreateCardCommand command(CardBehavior behavior) {
        return new CreateCardCommand(new UserId(1L), BankName.GALICIA,
                CardBrand.VISA, CardType.PLATINUM, behavior,
                "1234", LocalDate.now().plusYears(2), 20, 10);
    }

    @Test
    void create_persistsCreditCard() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(new Bank(BankName.GALICIA, null)));
        when(cardRepository.existsByBankNameAndBrandAndTypeAndCardNumber(any(), any(), any(), any())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = useCase.execute(command(CardBehavior.CREDIT));

        assertThat(result).isInstanceOf(CreditCard.class);
        assertThat(result.cardNumber()).isEqualTo("1234");
    }

    @Test
    void create_persistsDebitCardForInstantPayment() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(new Bank(BankName.GALICIA, null)));
        when(cardRepository.existsByBankNameAndBrandAndTypeAndCardNumber(any(), any(), any(), any())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = useCase.execute(command(CardBehavior.INSTANT_PAYMENT));

        assertThat(result).isInstanceOf(DebitCard.class);
    }

    @Test
    void create_rejectsDuplicate() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.of(new Bank(BankName.GALICIA, null)));
        when(cardRepository.existsByBankNameAndBrandAndTypeAndCardNumber(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command(CardBehavior.CREDIT)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_throwsWhenBankMissing() {
        when(bankRepository.findByName(BankName.GALICIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(CardBehavior.CREDIT)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
