package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.accountTypes.CheckingAccount;
import com.financialapp.banks.domain.model.account.accountTypes.InvestmentAccount;
import com.financialapp.banks.domain.model.account.accountTypes.SavingsAccount;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.infrastructure.persistence.entity.AccountJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountPersistenceMapperTest {

    private final AccountPersistenceMapper mapper = new AccountPersistenceMapper();

    private static final String CBU = "0070001600000000123459";
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private BankJpaEntity bankEntity() {
        return BankJpaEntity.builder().id(1L).bankNumber("007").name("GALICIA").build();
    }

    private AccountJpaEntity entity(String type) {
        return AccountJpaEntity.builder()
                .cbu(CBU).alias("alias").bank(bankEntity()).userId(1L).name("Main").type(type)
                .balance(new BigDecimal("100.00")).currency("ARS").isActive(true).createdAt(T0).updatedAt(T0)
                .build();
    }

    @ParameterizedTest
    @CsvSource({"CHECKING", "SAVINGS", "INVESTMENT"})
    void toDomain_mapsEachAccountType(String type) {
        // Given an account entity of the given type / When mapped
        Account account = mapper.toDomain(entity(type));

        // Then the right subtype and fields result
        assertThat(account.cbu().value()).isEqualTo(CBU);
        assertThat(account.balance().amount()).isEqualByComparingTo("100.00");
        switch (type) {
            case "CHECKING" -> assertThat(account).isInstanceOf(CheckingAccount.class);
            case "SAVINGS" -> assertThat(account).isInstanceOf(SavingsAccount.class);
            default -> assertThat(account).isInstanceOf(InvestmentAccount.class);
        }
    }

    @Test
    void toDomain_returnsNull_whenEntityNull() {
        // Given a null entity / When mapped / Then null (null guard)
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toJpa_mapsDomainToEntity() {
        // Given a checking account
        Account account = new CheckingAccount(Cbu.from(CBU), "alias", new Money(new BigDecimal("100.00"),
                java.util.Currency.getInstance("ARS")), new UserId(1L), new BankNumber("007"), "Main", true, T0, T0);

        // When mapped to a JPA entity
        AccountJpaEntity entity = mapper.toJpa(account, bankEntity());

        // Then the type discriminator and fields are set
        assertThat(entity.getType()).isEqualTo("CHECKING");
        assertThat(entity.getCbu()).isEqualTo(CBU);
        assertThat(entity.getCurrency()).isEqualTo("ARS");
    }

    @Test
    void toJpa_returnsNull_whenAccountNull() {
        // Given a null account / When mapped / Then null (null guard)
        assertThat(mapper.toJpa(null, bankEntity())).isNull();
    }

    @Test
    void merge_updatesExistingEntityInPlace() {
        // Given an existing entity and a savings account with new values
        AccountJpaEntity existing = entity("CHECKING");
        Account account = new SavingsAccount(Cbu.from(CBU), "alias", new Money(new BigDecimal("250.00"),
                java.util.Currency.getInstance("USD")), new UserId(1L), new BankNumber("007"), "Renamed", false, T0, T0);

        // When merged
        AccountJpaEntity merged = mapper.merge(existing, account, bankEntity());

        // Then the existing entity reflects the new state
        assertThat(merged).isSameAs(existing);
        assertThat(merged.getType()).isEqualTo("SAVINGS");
        assertThat(merged.getName()).isEqualTo("Renamed");
        assertThat(merged.getCurrency()).isEqualTo("USD");
        assertThat(merged.getIsActive()).isFalse();
    }

    @Test
    void toJpa_mapsInvestmentAccountType() {
        // Given an investment account (covers the INVESTMENT branch of resolveType)
        Account account = new InvestmentAccount(Cbu.from(CBU), "alias", new Money(new BigDecimal("100.00"),
                java.util.Currency.getInstance("ARS")), new UserId(1L), new BankNumber("007"), "Inv", true, T0, T0);

        // When mapped to a JPA entity
        AccountJpaEntity entity = mapper.toJpa(account, bankEntity());

        // Then the INVESTMENT discriminator is set
        assertThat(entity.getType()).isEqualTo("INVESTMENT");
    }

    @Test
    void resolveType_throwsForUnknownSubtype() {
        // Given a test-only Account subtype / When mapped to JPA / Then resolveType rejects it
        Account fake = new FakeAccount();
        assertThatThrownBy(() -> mapper.toJpa(fake, bankEntity()))
                .isInstanceOf(InfrastructureException.class)
                .hasMessageContaining("Unknown account subtype");
    }

    /** Test-only Account subtype to exercise resolveType's defensive throw. */
    private static final class FakeAccount extends Account {
        FakeAccount() {
            super(Cbu.from(CBU), "alias", new Money(new BigDecimal("1.00"), java.util.Currency.getInstance("ARS")),
                    new UserId(1L), new BankNumber("007"), "Fake", true, T0, T0);
        }
        @Override
        public Account withBalance(Money newBalance, LocalDateTime updatedAt) {
            return this;
        }
    }
}
