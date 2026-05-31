package com.financialapp.banks.application.card;

import com.financialapp.banks.domain.usecase.card.command.IssueCardCommand;
import com.financialapp.banks.application.card.impl.IssueCardUseCaseImpl;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceAlreadyExistsException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
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

import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueCardUseCaseImplTest {

    @Mock CardRepository cardRepository;
    @Mock BankRepository bankRepository;
    IssueCardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new IssueCardUseCaseImpl(cardRepository, bankRepository);
    }

    private IssueCardCommand command(CardBehavior behavior) {
        return new IssueCardCommand(new UserId(1L), new BankNumber("007"),
                CardBrand.VISA, CardType.PLATINUM, behavior,
                "1234567890123456", YearMonth.now().plusYears(2), 20, 10);
    }

    @Test
    void create_persistsCreditCard() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(cardRepository.findByCardNumber(any())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = useCase.execute(command(CardBehavior.CREDIT));

        assertThat(result).isInstanceOf(CreditCard.class);
        assertThat(result.cardNumber().value()).isEqualTo("1234567890123456");
    }

    @Test
    void create_persistsDebitCardForInstantPayment() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(cardRepository.findByCardNumber(any())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card result = useCase.execute(command(CardBehavior.INSTANT_PAYMENT));

        assertThat(result).isInstanceOf(DebitCard.class);
    }

    @Test
    void create_rejectsDuplicate() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.of(new Bank(new BankNumber("007"), "GALICIA", null)));
        when(cardRepository.findByCardNumber(any())).thenReturn(Optional.of(mock(Card.class)));

        assertThatThrownBy(() -> useCase.execute(command(CardBehavior.CREDIT)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_throwsWhenBankMissing() {
        when(bankRepository.findByBankNumber(new BankNumber("007"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(CardBehavior.CREDIT)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
