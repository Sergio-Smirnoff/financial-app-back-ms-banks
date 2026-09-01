package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.model.fee.AccountFeeScheduleId;
import com.financialapp.banks.infrastructure.persistence.entity.AccountFeeScheduleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class AccountFeeSchedulePersistenceMapper {

    public AccountFeeSchedule toDomain(AccountFeeScheduleJpaEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.getInstance(entity.getCurrency() != null ? entity.getCurrency() : "ARS");
        Money maintenanceFee = entity.getMaintenanceFee() != null ? new Money(entity.getMaintenanceFee(), currency) : null;
        Money transferFee = entity.getTransferFee() != null ? new Money(entity.getTransferFee(), currency) : null;

        return new AccountFeeSchedule(
                new AccountFeeScheduleId(entity.getId()),
                Cbu.from(entity.getAccountCbu()),
                maintenanceFee,
                transferFee,
                entity.getIvaTreatment()
        );
    }

    public AccountFeeScheduleJpaEntity toJpa(AccountFeeSchedule schedule) {
        if (schedule == null) return null;

        String currencyCode = "ARS";
        if (schedule.maintenanceFee() != null) {
            currencyCode = schedule.maintenanceFee().currency().getCurrencyCode();
        } else if (schedule.transferFee() != null) {
            currencyCode = schedule.transferFee().currency().getCurrencyCode();
        }

        return AccountFeeScheduleJpaEntity.builder()
                .id(schedule.id() != null ? schedule.id().value() : null)
                .accountCbu(schedule.accountCbu().value())
                .maintenanceFee(schedule.maintenanceFee() != null ? schedule.maintenanceFee().amount() : null)
                .transferFee(schedule.transferFee() != null ? schedule.transferFee().amount() : null)
                .ivaTreatment(schedule.ivaTreatment())
                .currency(currencyCode)
                .build();
    }

    public AccountFeeScheduleJpaEntity merge(AccountFeeScheduleJpaEntity existing, AccountFeeSchedule schedule) {
        String currencyCode = "ARS";
        if (schedule.maintenanceFee() != null) {
            currencyCode = schedule.maintenanceFee().currency().getCurrencyCode();
        } else if (schedule.transferFee() != null) {
            currencyCode = schedule.transferFee().currency().getCurrencyCode();
        }

        existing.setMaintenanceFee(schedule.maintenanceFee() != null ? schedule.maintenanceFee().amount() : null);
        existing.setTransferFee(schedule.transferFee() != null ? schedule.transferFee().amount() : null);
        existing.setIvaTreatment(schedule.ivaTreatment());
        existing.setCurrency(currencyCode);
        return existing;
    }
}
