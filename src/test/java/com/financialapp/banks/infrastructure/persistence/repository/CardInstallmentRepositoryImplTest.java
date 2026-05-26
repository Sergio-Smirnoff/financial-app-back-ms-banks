package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.infrastructure.persistence.entity.CardInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.CardInstallmentJpaRepository;
import com.financialapp.banks.infrastructure.persistence.jpa.CardJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.CardInstallmentPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardInstallmentRepositoryImplTest {

    @Mock CardInstallmentJpaRepository jpaRepository;
    @Mock CardJpaRepository cardJpaRepository;
    @Mock CardInstallmentPersistenceMapper mapper;
    @InjectMocks CardInstallmentRepositoryImpl repository;

    private static final String CARD_NUMBER = "1234567890123456";

    @Test
    void save_newInstallmentWithNullIdValue_insertsWithoutFindById() {
        Money money = new Money(new BigDecimal("1000.00"), Currency.getInstance("USD"));
        CardInstallment newInstallment = new CardInstallment(
                new CardInstallmentId(null), CARD_NUMBER, "Mac", money,
                1, 1, money, LocalDate.now(), false, null,
                LocalDateTime.now(), LocalDateTime.now());

        CardJpaEntity card = mock(CardJpaEntity.class);
        CardInstallmentJpaEntity entity = mock(CardInstallmentJpaEntity.class);
        when(cardJpaRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.of(card));
        when(mapper.toJpa(newInstallment, card)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(newInstallment);

        repository.save(newInstallment);

        verify(jpaRepository, never()).findById(any());
        verify(mapper).toJpa(newInstallment, card);
    }
}
