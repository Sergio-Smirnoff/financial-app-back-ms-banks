package com.financialapp.banks.domain.usecase.card.command;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.usecase.card.command.ImportCardExpensesCommand.ImportedExpense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImportCardExpensesCommandTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void exposesFieldsIncludingNestedExpenses() {
        var expense = new ImportedExpense("Coffee", new Money(new BigDecimal("100.00"), ARS), LocalDate.of(2026, 5, 1));
        var command = new ImportCardExpensesCommand("4111111111111111", new UserId(1L),
                "0070000000000000000001", "0070000000000000000002", List.of(expense));

        assertThat(command.cardNumber()).isEqualTo("4111111111111111");
        assertThat(command.userId()).isEqualTo(new UserId(1L));
        assertThat(command.arsAccountCbu()).isEqualTo("0070000000000000000001");
        assertThat(command.usdAccountCbu()).isEqualTo("0070000000000000000002");
        assertThat(command.expenses()).containsExactly(expense);
    }

    @Test
    void nestedImportedExpenseValueSemantics() {
        var a = new ImportedExpense("Coffee", new Money(new BigDecimal("100.00"), ARS), LocalDate.of(2026, 5, 1));
        var b = new ImportedExpense("Coffee", new Money(new BigDecimal("100.00"), ARS), LocalDate.of(2026, 5, 1));
        var c = new ImportedExpense("Tea", new Money(new BigDecimal("100.00"), ARS), LocalDate.of(2026, 5, 1));

        assertThat(a.description()).isEqualTo("Coffee");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(c);
        assertThat(a.toString()).contains("Coffee");
    }
}
