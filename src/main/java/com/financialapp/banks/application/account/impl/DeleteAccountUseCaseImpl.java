package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.DeleteAccountCommand;
import com.financialapp.banks.application.account.usecase.DeleteAccountUseCase;
import com.financialapp.banks.domain.exception.BusinessException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.port.InvestmentsPort;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteAccountUseCaseImpl implements DeleteAccountUseCase {

    private final AccountRepository accountRepository;
    private final InvestmentsPort investmentsPort;

    @Override
    @Transactional
    public void execute(DeleteAccountCommand command) {
        Account account = accountRepository.findByCbuAndBankName(command.cbu(), command.bankName())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + command.cbu()));

        if (account.type() == AccountType.INVESTMENT) {
            try {
                int holdings = investmentsPort.countHoldings(account.cbu());
                if (holdings > 0) {
                    throw new BusinessException("Cannot delete investment account with active holdings. Sell or delete holdings first.");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to check holdings for account {}: {}", command.cbu(), e.getMessage());
                throw new BusinessException("Safety check failed: could not verify active holdings. Try again later.");
            }
        } else if (account.balance().amount().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Cannot delete account with non-zero balance: " + account.balance());
        }

        accountRepository.delete(account.cbu());
    }
}
