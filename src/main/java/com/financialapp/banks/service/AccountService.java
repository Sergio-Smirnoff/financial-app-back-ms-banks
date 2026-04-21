package com.financialapp.banks.service;

import com.financialapp.banks.client.InvestmentsClient;
import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.exception.ResourceNotFoundException;
import com.financialapp.banks.kafka.event.BankAlertEvent;
import com.financialapp.banks.kafka.producer.BanksEventProducer;
import com.financialapp.banks.mapper.AccountMapper;
import com.financialapp.banks.model.dto.request.AccountRequest;
import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.enums.AccountType;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final AccountMapper accountMapper;
    private final InvestmentsClient investmentsClient;
    private final BanksEventProducer eventProducer;

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");

    @Transactional(readOnly = true)
    public List<AccountResponse> listByUser(Long userId) {
        return accountRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id, Long userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        return mapToResponse(account);
    }

    @Transactional
    public void adjustBalance(Long id, BigDecimal delta, String expectedCurrency) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        
        if (account.getType() == AccountType.INVESTMENT) {
            throw new BusinessException("Cannot manually adjust balance of an investment account. It is automatically calculated from holdings.");
        }

        if (expectedCurrency != null && !account.getCurrency().equalsIgnoreCase(expectedCurrency)) {
            throw new BusinessException("Currency mismatch: account is " + account.getCurrency() + " but operation is " + expectedCurrency);
        }

        BigDecimal newBalance = account.getBalance().add(delta);
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient funds in account: " + account.getName());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        // 1. Check for low balance
        if (newBalance.compareTo(LOW_BALANCE_THRESHOLD) < 0) {
            eventProducer.sendBankAlert(BankAlertEvent.builder()
                    .userId(account.getUserId())
                    .type("LOW_BALANCE")
                    .title("Low Account Balance")
                    .message(String.format("Your account '%s' has a low balance of %s %s.",
                            account.getName(), newBalance, account.getCurrency()))
                    .metadata(String.format("{\"accountId\":%d,\"bankId\":%d}", account.getId(), account.getBankId()))
                    .build());
        }

        // 2. Generic Transaction notification
        String type = delta.compareTo(BigDecimal.ZERO) >= 0 ? "TRANSFER_RECEIVED" : "TRANSFER_SENT";
        String title = delta.compareTo(BigDecimal.ZERO) >= 0 ? "Funds Received" : "Funds Sent";
        String action = delta.compareTo(BigDecimal.ZERO) >= 0 ? "credited to" : "debited from";
        
        eventProducer.sendBankAlert(BankAlertEvent.builder()
                .userId(account.getUserId())
                .type(type)
                .title(title)
                .message(String.format("%s %s has been %s your account '%s'.",
                        delta.abs(), account.getCurrency(), action, account.getName()))
                .metadata(String.format("{\"accountId\":%d,\"bankId\":%d,\"amount\":%s}", 
                        account.getId(), account.getBankId(), delta.abs()))
                .build());
    }

    @Transactional
    public AccountResponse create(Long userId, AccountRequest request) {
        Bank bank = bankRepository.findByIdAndUserId(request.bankId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + request.bankId()));
        if (accountRepository.existsByBankIdAndName(bank.getId(), request.name())) {
            throw new BusinessException(
                    "An account with name '" + request.name() + "' already exists in this bank");
        }

        if (request.type() == AccountType.INVESTMENT && accountRepository.existsByBankIdAndType(bank.getId(), AccountType.INVESTMENT)) {
            throw new BusinessException("This bank already has an investment account. Only one is allowed per bank.");
        }

        Account account = Account.builder()
                .bankId(bank.getId())
                .userId(userId)
                .name(request.name())
                .type(request.type())
                .balance(request.balance() != null ? request.balance() : BigDecimal.ZERO)
                .currency(request.currency().toUpperCase())
                .isActive(request.isActive() == null ? Boolean.TRUE : request.isActive())
                .build();
        return mapToResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, Long userId, AccountRequest request) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        if (!account.getBankId().equals(request.bankId())) {
            throw new BusinessException("Changing an account's bank is not supported");
        }
        if (!account.getName().equals(request.name())
                && accountRepository.existsByBankIdAndName(account.getBankId(), request.name())) {
            throw new BusinessException(
                    "An account with name '" + request.name() + "' already exists in this bank");
        }
        account.setName(request.name());
        account.setType(request.type());
        account.setBalance(request.balance());
        account.setCurrency(request.currency().toUpperCase());
        if (request.isActive() != null) {
            account.setIsActive(request.isActive());
        }
        return mapToResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        
        if (account.getType() == AccountType.INVESTMENT) {
            try {
                var response = investmentsClient.countHoldings(id);
                if (response.getData() != null && response.getData() > 0) {
                    throw new BusinessException("Cannot delete investment account because it still has active holdings. Sell or delete the holdings first.");
                }
            } catch (Exception e) {
                if (e instanceof BusinessException) throw e;
                log.error("Failed to check holdings for account {}: {}", id, e.getMessage());
                throw new BusinessException("Safety check failed: Could not verify if account has active holdings. Please try again later.");
            }
        }

        // 1. Check balance is zero (for non-investment accounts)
        if (account.getType() != AccountType.INVESTMENT && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Cannot delete account with non-zero balance: " + account.getBalance());
        }

        accountRepository.delete(account);
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = accountMapper.toResponse(account);
        if (account.getType() == AccountType.INVESTMENT) {
            try {
                var valuation = investmentsClient.getValuation(account.getId()).getData();
                if (valuation != null && valuation.totalValuation() != null) {
                    return response.withBalance(valuation.totalValuation());
                }
            } catch (Exception e) {
                log.error("Failed to fetch valuation for investment account {}: {}", account.getId(), e.getMessage());
            }
        }
        return response;
    }
}
