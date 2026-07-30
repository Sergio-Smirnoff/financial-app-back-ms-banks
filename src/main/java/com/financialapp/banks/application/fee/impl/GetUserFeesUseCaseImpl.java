package com.financialapp.banks.application.fee.impl;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.repository.AccountFeeScheduleRepository;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.repository.CardFeeScheduleRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.service.DebitCreditTax;
import com.financialapp.banks.domain.usecase.fee.GetUserFees;
import com.financialapp.banks.domain.usecase.fee.response.UserFeesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserFeesUseCaseImpl implements GetUserFees {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final AccountFeeScheduleRepository accountFeeScheduleRepository;
    private final CardFeeScheduleRepository cardFeeScheduleRepository;
    private final DebitCreditTax debitCreditTax = new DebitCreditTax();

    @Override
    @Transactional(readOnly = true)
    public UserFeesResult execute(UserId userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        Map<String, AccountFeeSchedule> accountSchedules = accountFeeScheduleRepository.findByOwner(userId).stream()
                .collect(Collectors.toMap(s -> s.accountCbu().value(), Function.identity(), (s1, s2) -> s2));

        List<Card> cards = cardRepository.findByUserId(userId);
        Map<String, CardFeeSchedule> cardSchedules = cardFeeScheduleRepository.findByOwner(userId).stream()
                .collect(Collectors.toMap(s -> s.cardNumber().value(), Function.identity(), (s1, s2) -> s2));

        List<UserFeesResult.AccountFeesEntry> accountEntries = new ArrayList<>();
        for (Account account : accounts) {
            String taxRate = debitCreditTax.rate(account.type()).toPlainString();
            AccountFeeSchedule s = accountSchedules.get(account.cbu().value());
            if (s != null) {
                String mainFee = s.maintenanceFee() != null ? s.maintenanceFee().amount().toPlainString() : null;
                String transFee = s.transferFee() != null ? s.transferFee().amount().toPlainString() : null;
                String currency = s.maintenanceFee() != null ? s.maintenanceFee().currency().getCurrencyCode()
                        : (s.transferFee() != null ? s.transferFee().currency().getCurrencyCode() : "ARS");
                String iva = s.ivaTreatment() != null ? s.ivaTreatment().name() : null;
                accountEntries.add(new UserFeesResult.AccountFeesEntry(
                        account.cbu().value(), account.type().name(), mainFee, transFee, currency, iva, taxRate));
            } else {
                accountEntries.add(new UserFeesResult.AccountFeesEntry(
                        account.cbu().value(), account.type().name(), null, null, "ARS", null, taxRate));
            }
        }

        List<UserFeesResult.CardFeesEntry> cardEntries = new ArrayList<>();
        for (Card card : cards) {
            CardFeeSchedule s = cardSchedules.get(card.cardNumber().value());
            if (s != null) {
                String annualFee = s.annualFee() != null ? s.annualFee().amount().toPlainString() : null;
                String surcharge = s.internationalSurchargePct() != null ? s.internationalSurchargePct().toPlainString() : null;
                String currency = s.annualFee() != null ? s.annualFee().currency().getCurrencyCode() : "ARS";
                String iva = s.ivaTreatment() != null ? s.ivaTreatment().name() : null;
                cardEntries.add(new UserFeesResult.CardFeesEntry(
                        card.cardNumber().value(), annualFee, surcharge, currency, iva));
            } else {
                cardEntries.add(new UserFeesResult.CardFeesEntry(
                        card.cardNumber().value(), null, null, "ARS", null));
            }
        }

        return new UserFeesResult(accountEntries, cardEntries);
    }
}
