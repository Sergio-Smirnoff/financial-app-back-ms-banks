package com.financialapp.banks.application.bank.impl;

import com.financialapp.banks.application.bank.usecase.GetBankUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBankUseCaseImpl implements GetBankUseCase {

    private final BankRepository bankRepository;

    @Override
    @Transactional(readOnly = true)
    public Bank execute(BankName name) {
        return bankRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", name.getDisplayName()));
    }
}
