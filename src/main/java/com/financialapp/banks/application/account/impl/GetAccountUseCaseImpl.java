package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.usecase.GetAccountUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountUseCaseImpl implements GetAccountUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Account execute(String cbu) {
        return accountRepository.findByCbu(cbu)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + cbu));
    }
}
