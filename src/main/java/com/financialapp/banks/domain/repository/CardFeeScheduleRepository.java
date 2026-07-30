package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardNumber;
import com.financialapp.banks.domain.model.fee.CardFeeSchedule;

import java.util.List;
import java.util.Optional;

public interface CardFeeScheduleRepository {
    CardFeeSchedule save(CardFeeSchedule schedule);
    Optional<CardFeeSchedule> findByCardNumber(CardNumber cardNumber);
    List<CardFeeSchedule> findByOwner(UserId userId);
}
