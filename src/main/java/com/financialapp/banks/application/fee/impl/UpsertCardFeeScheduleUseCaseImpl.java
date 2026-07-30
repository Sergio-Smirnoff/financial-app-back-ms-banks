package com.financialapp.banks.application.fee.impl;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;
import com.financialapp.banks.domain.repository.CardFeeScheduleRepository;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.usecase.fee.UpsertCardFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.command.UpsertCardFeeScheduleCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
@RequiredArgsConstructor
public class UpsertCardFeeScheduleUseCaseImpl implements UpsertCardFeeSchedule {

    private final CardRepository cardRepository;
    private final CardFeeScheduleRepository feeScheduleRepository;

    @Override
    @Transactional
    public CardFeeSchedule execute(UpsertCardFeeScheduleCommand cmd) {
        Card card = cardRepository.findByCardNumber(cmd.cardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card", cmd.cardNumber()));

        if (!card.userId().equals(cmd.userId())) {
            throw new ResourceNotFoundException("Card", cmd.cardNumber());
        }

        Currency currency = Currency.getInstance(cmd.currency() != null ? cmd.currency() : "ARS");
        Money annualFee = cmd.annualFee() != null ? new Money(cmd.annualFee(), currency) : null;

        CardFeeSchedule schedule = new CardFeeSchedule(
                null,
                CardNumber.from(cmd.cardNumber()),
                annualFee,
                cmd.internationalSurchargePct(),
                cmd.ivaTreatment()
        );

        return feeScheduleRepository.save(schedule);
    }
}
