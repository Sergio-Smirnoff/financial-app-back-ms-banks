package com.financialapp.banks.application.fee;

import com.financialapp.banks.application.fee.impl.GetUserFeesUseCaseImpl;
import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBilling;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardDetails;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.model.fee.AccountFeeScheduleId;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.model.fee.CardFeeScheduleId;
import com.financialapp.banks.domain.repository.AccountFeeScheduleRepository;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardFeeScheduleRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.fee.response.UserFeesResult;
import com.financialapp.commons.core.domain.model.IvaTreatment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserFeesUseCaseImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CardRepository cardRepository;
    @Mock private AccountFeeScheduleRepository accountFeeScheduleRepository;
    @Mock private CardFeeScheduleRepository cardFeeScheduleRepository;

    private GetUserFeesUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);
    private static final Cbu CBU_CHECKING = Cbu.from("0070001600000000123459");
    private static final Cbu CBU_SAVINGS = Cbu.from("0070001600000000123459");
    private static final Currency ARS = Currency.getInstance("ARS");

    @BeforeEach
    void setUp() {
        useCase = new GetUserFeesUseCaseImpl(accountRepository, cardRepository, accountFeeScheduleRepository, cardFeeScheduleRepository);
    }

    @Test
    void execute_computesTaxRateAndIncludesUnconfiguredAccountsAndCards() {
        Account checking = Account.create(
                AccountType.CHECKING, CBU_CHECKING, "checking",
                new Money(BigDecimal.TEN, ARS), USER_ID, new BankNumber("007"), "Checking", true,
                LocalDateTime.now(), LocalDateTime.now());

        AccountFeeSchedule schedule = new AccountFeeSchedule(
                new AccountFeeScheduleId(10L), CBU_CHECKING,
                new Money(new BigDecimal("4500.00"), ARS), null, IvaTreatment.SEPARATE);

        Card card = Card.create("4111111111111111", USER_ID, new BankNumber("007"),
                new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT, YearMonth.now().plusYears(1), new CardBilling(15, 5), null),
                LocalDateTime.now(), LocalDateTime.now());

        when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(checking));
        when(accountFeeScheduleRepository.findByOwner(USER_ID)).thenReturn(List.of(schedule));
        when(cardRepository.findByUserId(USER_ID)).thenReturn(List.of(card));
        when(cardFeeScheduleRepository.findByOwner(USER_ID)).thenReturn(List.of());

        UserFeesResult result = useCase.execute(USER_ID);

        assertThat(result.accounts()).hasSize(1);
        var accEntry = result.accounts().get(0);
        assertThat(accEntry.cbu()).isEqualTo(CBU_CHECKING.value());
        assertThat(accEntry.accountType()).isEqualTo("CHECKING");
        assertThat(accEntry.maintenanceFee()).isEqualTo("4500.00");
        assertThat(accEntry.debitCreditTaxRate()).isEqualTo("0.006");

        assertThat(result.cards()).hasSize(1);
        var cardEntry = result.cards().get(0);
        assertThat(cardEntry.cardNumber()).isEqualTo("4111111111111111");
        assertThat(cardEntry.annualFee()).isNull();
    }
}
