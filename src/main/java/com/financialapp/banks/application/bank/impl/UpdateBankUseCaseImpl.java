package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.command.UpdateBankCommand;
import com.financialapp.banks.application.bank.usecase.UpdateBankUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBankUseCaseImpl implements UpdateBankUseCase {

    private final BankRepository bankRepository;

    @Override
    @Transactional
    public Bank execute(UpdateBankCommand command) {
        Bank existing = bankRepository.findByName(command.name())
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + command.name()));
        Bank updated = new Bank(existing.name(), command.logo());
        return bankRepository.save(updated);
    }
}
