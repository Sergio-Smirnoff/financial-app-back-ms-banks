package com.financialapp.banks.application.account.impl;

import com.financialapp.banks.application.account.command.DeleteAccountCommand;
import com.financialapp.banks.application.account.usecase.DeleteAccountUseCase;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.exception.ResourceConflictException;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.port.InvestmentsPort;
import com.financialapp.banks.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

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
                .orElseThrow(() -> new ResourceNotFoundException("Account", command.cbu()));

        if (account instanceof InvestmentAccount) {
            try {
                int holdings = investmentsPort.countHoldings(account.cbu());
                if (holdings > 0) {
                    throw new ResourceConflictException(
                        DomainError.ACCOUNT_NOT_DELETABLE,
                        "Cannot delete account '" + command.cbu() + "': investment account has active holdings",
                        Map.of("cbu", command.cbu(), "reason", "active holdings"));
                }
            } catch (DomainException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to check holdings for account {}: {}", command.cbu(), e.getMessage());
                throw new InfrastructureException("Safety check failed: could not verify active holdings. Try again later.");
            }
        } else if (account.balance().amount().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResourceConflictException(
                DomainError.ACCOUNT_NOT_DELETABLE,
                "Cannot delete account '" + command.cbu() + "': non-zero balance",
                Map.of("cbu", command.cbu(), "reason", "non-zero balance"));
        }

        accountRepository.delete(account.cbu());
    }
}
