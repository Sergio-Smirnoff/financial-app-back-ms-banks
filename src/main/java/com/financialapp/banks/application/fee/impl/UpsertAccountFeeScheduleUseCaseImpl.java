package com.financialapp.banks.application.fee.impl;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.fee.AccountFeeSchedule;
import com.financialapp.banks.domain.repository.AccountFeeScheduleRepository;
import com.financialapp.banks.domain.repository.AccountRepository;
import com.financialapp.banks.domain.usecase.fee.UpsertAccountFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.command.UpsertAccountFeeScheduleCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
@RequiredArgsConstructor
public class UpsertAccountFeeScheduleUseCaseImpl implements UpsertAccountFeeSchedule {

    private final AccountRepository accountRepository;
    private final AccountFeeScheduleRepository feeScheduleRepository;

    @Override
    @Transactional
    public AccountFeeSchedule execute(UpsertAccountFeeScheduleCommand cmd) {
        Account account = accountRepository.findByCbu(cmd.cbu())
                .orElseThrow(() -> new ResourceNotFoundException("Account", cmd.cbu()));

        if (!account.userId().equals(cmd.userId())) {
            throw new ResourceNotFoundException("Account", cmd.cbu());
        }

        Currency currency = Currency.getInstance(cmd.currency() != null ? cmd.currency() : "ARS");
        Money maintenanceFee = cmd.maintenanceFee() != null ? new Money(cmd.maintenanceFee(), currency) : null;
        Money transferFee = cmd.transferFee() != null ? new Money(cmd.transferFee(), currency) : null;

        AccountFeeSchedule schedule = new AccountFeeSchedule(
                null,
                Cbu.from(cmd.cbu()),
                maintenanceFee,
                transferFee,
                cmd.ivaTreatment()
        );

        return feeScheduleRepository.save(schedule);
    }
}
