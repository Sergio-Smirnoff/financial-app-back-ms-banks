package com.financialapp.banks.application.card.impl;

import com.financialapp.banks.application.card.command.CreateCardExpenseCommand;
import com.financialapp.banks.application.card.command.ImportCardExpensesCommand;
import com.financialapp.banks.application.card.command.PayCardInstallmentCommand;
import com.financialapp.banks.application.card.usecase.BatchImportResult;
import com.financialapp.banks.application.card.usecase.ImportCardExpensesUseCase;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportCardExpensesUseCaseImpl implements ImportCardExpensesUseCase {

    private final CardRepository cardRepository;
    private final CreateCardExpenseUseCaseImpl createExpense;
    private final PayCardInstallmentUseCaseImpl payInstallment;

    @Override
    @Transactional
    public BatchImportResult execute(ImportCardExpensesCommand cmd) {
        cardRepository.findByIdAndUserId(cmd.cardId(), cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cmd.cardId().value()));

        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();

        for (ImportCardExpensesCommand.ImportedExpense expense : cmd.expenses()) {
            AccountId accountId = resolveAccount(expense.currency(), cmd.arsAccountId(), cmd.usdAccountId());
            if (accountId == null) {
                skipped++;
                continue;
            }
            try {
                List<CardInstallment> created = createExpense.execute(new CreateCardExpenseCommand(
                        cmd.cardId(), cmd.userId(), expense.description(), expense.amount(),
                        expense.currency(), 1, expense.date()));
                payInstallment.execute(new PayCardInstallmentCommand(
                        cmd.cardId(), created.get(0).id(), cmd.userId(), accountId,
                        expense.date(), cmd.bypassBalance()));
                imported++;
            } catch (Exception e) {
                errors.add(expense.description() + ": " + e.getMessage());
            }
        }

        return new BatchImportResult(imported, skipped, errors);
    }

    private AccountId resolveAccount(String currency, AccountId arsId, AccountId usdId) {
        return switch (currency.toUpperCase()) {
            case "ARS" -> arsId;
            case "USD" -> usdId;
            default -> null;
        };
    }
}
