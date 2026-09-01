package com.financialapp.banks.web.mapper;

import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.response.UserFeesResult;
import com.financialapp.banks.web.dto.response.AccountFeeScheduleResponse;
import com.financialapp.banks.web.dto.response.CardFeeScheduleResponse;
import com.financialapp.banks.web.dto.response.UserFeesResponse;
import org.springframework.stereotype.Component;

@Component
public class FeeWebMapper {

    public AccountFeeScheduleResponse toResponse(AccountFeeSchedule schedule) {
        if (schedule == null) return null;
        String mainFee = schedule.maintenanceFee() != null ? schedule.maintenanceFee().amount().toPlainString() : null;
        String transFee = schedule.transferFee() != null ? schedule.transferFee().amount().toPlainString() : null;
        String currency = schedule.maintenanceFee() != null ? schedule.maintenanceFee().currency().getCurrencyCode()
                : (schedule.transferFee() != null ? schedule.transferFee().currency().getCurrencyCode() : "ARS");

        return AccountFeeScheduleResponse.builder()
                .cbu(schedule.accountCbu().value())
                .maintenanceFee(mainFee)
                .transferFee(transFee)
                .currency(currency)
                .ivaTreatment(schedule.ivaTreatment())
                .build();
    }

    public CardFeeScheduleResponse toResponse(CardFeeSchedule schedule) {
        if (schedule == null) return null;
        String annualFee = schedule.annualFee() != null ? schedule.annualFee().amount().toPlainString() : null;
        String surcharge = schedule.internationalSurchargePct() != null ? schedule.internationalSurchargePct().toPlainString() : null;
        String currency = schedule.annualFee() != null ? schedule.annualFee().currency().getCurrencyCode() : "ARS";

        return CardFeeScheduleResponse.builder()
                .cardNumber(schedule.cardNumber().value())
                .annualFee(annualFee)
                .internationalSurchargePct(surcharge)
                .currency(currency)
                .ivaTreatment(schedule.ivaTreatment())
                .build();
    }

    public UserFeesResponse toResponse(UserFeesResult result) {
        if (result == null) return null;
        var accounts = result.accounts().stream()
                .map(a -> new UserFeesResponse.AccountFeesEntry(
                        a.cbu(), a.accountType(), a.maintenanceFee(), a.transferFee(), a.currency(), a.ivaTreatment(), a.debitCreditTaxRate()))
                .toList();
        var cards = result.cards().stream()
                .map(c -> new UserFeesResponse.CardFeesEntry(
                        c.cardNumber(), c.annualFee(), c.internationalSurchargePct(), c.currency(), c.ivaTreatment()))
                .toList();

        return UserFeesResponse.builder()
                .accounts(accounts)
                .cards(cards)
                .build();
    }
}
