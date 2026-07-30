package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.model.fee.CardFeeScheduleId;
import com.financialapp.banks.infrastructure.persistence.entity.CardFeeScheduleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CardFeeSchedulePersistenceMapper {

    public CardFeeSchedule toDomain(CardFeeScheduleJpaEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.getInstance(entity.getCurrency() != null ? entity.getCurrency() : "ARS");
        Money annualFee = entity.getAnnualFee() != null ? new Money(entity.getAnnualFee(), currency) : null;

        return new CardFeeSchedule(
                new CardFeeScheduleId(entity.getId()),
                CardNumber.from(entity.getCardNumber()),
                annualFee,
                entity.getInternationalSurchargePct(),
                entity.getIvaTreatment()
        );
    }

    public CardFeeScheduleJpaEntity toJpa(CardFeeSchedule schedule) {
        if (schedule == null) return null;

        String currencyCode = schedule.annualFee() != null ? schedule.annualFee().currency().getCurrencyCode() : "ARS";

        return CardFeeScheduleJpaEntity.builder()
                .id(schedule.id() != null ? schedule.id().value() : null)
                .cardNumber(schedule.cardNumber().value())
                .annualFee(schedule.annualFee() != null ? schedule.annualFee().amount() : null)
                .internationalSurchargePct(schedule.internationalSurchargePct())
                .ivaTreatment(schedule.ivaTreatment())
                .currency(currencyCode)
                .build();
    }

    public CardFeeScheduleJpaEntity merge(CardFeeScheduleJpaEntity existing, CardFeeSchedule schedule) {
        String currencyCode = schedule.annualFee() != null ? schedule.annualFee().currency().getCurrencyCode() : "ARS";

        existing.setAnnualFee(schedule.annualFee() != null ? schedule.annualFee().amount() : null);
        existing.setInternationalSurchargePct(schedule.internationalSurchargePct());
        existing.setIvaTreatment(schedule.ivaTreatment());
        existing.setCurrency(currencyCode);
        return existing;
    }
}
