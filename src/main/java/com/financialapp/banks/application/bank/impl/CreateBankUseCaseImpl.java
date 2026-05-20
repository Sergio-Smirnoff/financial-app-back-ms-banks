package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.command.CreateBankCommand;
import com.financialapp.banks.application.bank.usecase.CreateBankUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBankUseCaseImpl implements CreateBankUseCase {

    private final BankRepository bankRepository;

    @Override
    @Transactional
    public Bank execute(CreateBankCommand command) {
        if (bankRepository.existsByName(command.name())) {
            throw new BusinessException("Bank '" + command.name().getDisplayName() + "' already exists");
        }
        Bank bank = new Bank(command.name(), command.logo());
        return bankRepository.save(bank);
    }
}
