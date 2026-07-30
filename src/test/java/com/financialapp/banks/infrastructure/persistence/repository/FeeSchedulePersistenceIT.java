package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.*;
import com.financialapp.banks.domain.model.fee.*;
import com.financialapp.banks.domain.repository.AccountFeeScheduleRepository;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardFeeScheduleRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.commons.core.domain.model.IvaTreatment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeeSchedulePersistenceIT {

    @Autowired
    AccountFeeScheduleRepository accountFeeScheduleRepository;
    @Autowired
    CardFeeScheduleRepository cardFeeScheduleRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CardRepository cardRepository;

    private static final Cbu CBU = Cbu.from("0070001600000000123459");
    private static final String CARD_NUM = "4111111111111111";
    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void accountFeeSchedule_upsertTwice_updatesRowAndFindsByOwner() {
        UserId userId = new UserId(100L);
        Account account = Account.create(
                AccountType.CHECKING, CBU, "checking.acc",
                new Money(new BigDecimal("5000.00"), ARS), userId,
                new BankNumber("007"), "Galicia Checking", true,
                LocalDateTime.now(), LocalDateTime.now());
        accountRepository.save(account);

        AccountFeeSchedule schedule1 = new AccountFeeSchedule(
                null, CBU, new Money(new BigDecimal("4500.00"), ARS), null, IvaTreatment.SEPARATE);
        accountFeeScheduleRepository.save(schedule1);

        AccountFeeSchedule schedule2 = new AccountFeeSchedule(
                null, CBU, new Money(new BigDecimal("5000.00"), ARS), new Money(new BigDecimal("150.00"), ARS), IvaTreatment.INCLUDED);
        accountFeeScheduleRepository.save(schedule2);

        AccountFeeSchedule found = accountFeeScheduleRepository.findByAccountCbu(CBU).orElseThrow();
        assertThat(found.maintenanceFee().amount()).isEqualByComparingTo("5000.00");
        assertThat(found.transferFee().amount()).isEqualByComparingTo("150.00");
        assertThat(found.ivaTreatment()).isEqualTo(IvaTreatment.INCLUDED);

        var userSchedules = accountFeeScheduleRepository.findByOwner(userId);
        assertThat(userSchedules).hasSize(1);
        assertThat(userSchedules.get(0).accountCbu()).isEqualTo(CBU);
    }

    @Test
    void cardFeeSchedule_upsertTwice_updatesRowAndFindsByOwner() {
        UserId userId = new UserId(200L);
        CardDetails details = new CardDetails(CardBrand.VISA, CardType.PLATINUM, CardBehavior.CREDIT,
                YearMonth.now().plusYears(2), new CardBilling(20, 10), null);
        Card card = Card.create(CARD_NUM, userId, new BankNumber("007"), details,
                LocalDateTime.now(), LocalDateTime.now());
        cardRepository.save(card);

        CardNumber cardNumber = CardNumber.from(CARD_NUM);
        CardFeeSchedule schedule1 = new CardFeeSchedule(
                null, cardNumber, new Money(new BigDecimal("80000.00"), ARS), new BigDecimal("3.50"), IvaTreatment.SEPARATE);
        cardFeeScheduleRepository.save(schedule1);

        CardFeeSchedule schedule2 = new CardFeeSchedule(
                null, cardNumber, new Money(new BigDecimal("95000.00"), ARS), new BigDecimal("4.00"), IvaTreatment.EXEMPT);
        cardFeeScheduleRepository.save(schedule2);

        CardFeeSchedule found = cardFeeScheduleRepository.findByCardNumber(cardNumber).orElseThrow();
        assertThat(found.annualFee().amount()).isEqualByComparingTo("95000.00");
        assertThat(found.internationalSurchargePct()).isEqualByComparingTo("4.00");
        assertThat(found.ivaTreatment()).isEqualTo(IvaTreatment.EXEMPT);

        var userSchedules = cardFeeScheduleRepository.findByOwner(userId);
        assertThat(userSchedules).hasSize(1);
        assertThat(userSchedules.get(0).cardNumber()).isEqualTo(cardNumber);
    }
}
