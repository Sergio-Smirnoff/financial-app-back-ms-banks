package com.financialapp.banks.domain.model.fee;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.fee.InvalidFeeScheduleException;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.commons.core.domain.model.IvaTreatment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeScheduleTest {

    private static final Cbu CBU = Cbu.from("0070001600000000123459");
    private static final CardNumber CARD_NUMBER = CardNumber.from("4111111111111111");
    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void accountFeeSchedule_validCreationAndAccessors() {
        Money maintenance = new Money(new BigDecimal("4500.00"), ARS);
        Money transfer = new Money(new BigDecimal("100.00"), ARS);

        AccountFeeSchedule schedule = new AccountFeeSchedule(
                new AccountFeeScheduleId(1L), CBU, maintenance, transfer, IvaTreatment.SEPARATE);

        assertThat(schedule.id().value()).isEqualTo(1L);
        assertThat(schedule.accountCbu()).isEqualTo(CBU);
        assertThat(schedule.maintenanceFee()).isEqualTo(maintenance);
        assertThat(schedule.transferFee()).isEqualTo(transfer);
        assertThat(schedule.ivaTreatment()).isEqualTo(IvaTreatment.SEPARATE);
    }

    @Test
    void accountFeeSchedule_allowsNullFees() {
        AccountFeeSchedule schedule = new AccountFeeSchedule(
                new AccountFeeScheduleId(null), CBU, null, null, IvaTreatment.EXEMPT);

        assertThat(schedule.maintenanceFee()).isNull();
        assertThat(schedule.transferFee()).isNull();
    }

    @Test
    void accountFeeSchedule_nullCbuOrIva_throwsException() {
        assertThatThrownBy(() -> new AccountFeeSchedule(null, null, null, null, IvaTreatment.SEPARATE))
                .isInstanceOf(InvalidFeeScheduleException.class);

        assertThatThrownBy(() -> new AccountFeeSchedule(null, CBU, null, null, null))
                .isInstanceOf(InvalidFeeScheduleException.class);
    }

    @Test
    void cardFeeSchedule_validCreationAndAccessors() {
        Money annual = new Money(new BigDecimal("80000.00"), ARS);
        BigDecimal surcharge = new BigDecimal("3.50");

        CardFeeSchedule schedule = new CardFeeSchedule(
                new CardFeeScheduleId(1L), CARD_NUMBER, annual, surcharge, IvaTreatment.INCLUDED);

        assertThat(schedule.id().value()).isEqualTo(1L);
        assertThat(schedule.cardNumber()).isEqualTo(CARD_NUMBER);
        assertThat(schedule.annualFee()).isEqualTo(annual);
        assertThat(schedule.internationalSurchargePct()).isEqualTo(surcharge);
        assertThat(schedule.ivaTreatment()).isEqualTo(IvaTreatment.INCLUDED);
    }

    @Test
    void cardFeeSchedule_nullCardNumberOrIva_throwsException() {
        assertThatThrownBy(() -> new CardFeeSchedule(null, null, null, null, IvaTreatment.SEPARATE))
                .isInstanceOf(InvalidFeeScheduleException.class);

        assertThatThrownBy(() -> new CardFeeSchedule(null, CARD_NUMBER, null, null, null))
                .isInstanceOf(InvalidFeeScheduleException.class);
    }

    @Test
    void cardFeeSchedule_invalidSurchargeBounds_throwsException() {
        assertThatThrownBy(() -> new CardFeeSchedule(null, CARD_NUMBER, null, new BigDecimal("0.00"), IvaTreatment.SEPARATE))
                .isInstanceOf(InvalidFeeScheduleException.class);

        assertThatThrownBy(() -> new CardFeeSchedule(null, CARD_NUMBER, null, new BigDecimal("100.01"), IvaTreatment.SEPARATE))
                .isInstanceOf(InvalidFeeScheduleException.class);

        assertThatThrownBy(() -> new CardFeeSchedule(null, CARD_NUMBER, null, new BigDecimal("-1.00"), IvaTreatment.SEPARATE))
                .isInstanceOf(InvalidFeeScheduleException.class);
    }
}
