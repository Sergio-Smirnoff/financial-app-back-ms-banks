package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.kafka.producer.BanksEventProducer;
import com.financialapp.banks.mapper.CardMapper;
import com.financialapp.banks.model.dto.request.CardRequest;
import com.financialapp.banks.model.dto.response.CardResponse;
import com.financialapp.banks.model.entity.Account;
import com.financialapp.banks.model.entity.Bank;
import com.financialapp.banks.model.entity.Card;
import com.financialapp.banks.model.enums.CardBehavior;
import com.financialapp.banks.model.enums.CardBrand;
import com.financialapp.banks.model.enums.CardType;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.BankRepository;
import com.financialapp.banks.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock CardRepository cardRepository;
    @Mock AccountRepository accountRepository;
    @Mock BankRepository bankRepository;
    @Mock BanksEventProducer eventProducer;

    CardMapper cardMapper = new CardMapper() {};

    CardService service;

    @BeforeEach
    void setUp() {
        service = new CardService(cardRepository, accountRepository, bankRepository, cardMapper, eventProducer);
    }

    @Test
    void create_persistsCardForCurrentUser() {
        Bank bank = Bank.builder().id(10L).userId(1L).name("Chase").build();
        when(bankRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(bank));
        when(cardRepository.existsByBankIdAndBrandAndCardTypeAndLast4Digits(any(), any(), any(), any())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(500L);
            return c;
        });

        // For display name mapping
        when(bankRepository.findById(10L)).thenReturn(Optional.of(bank));

        CardRequest request = new CardRequest(10L, CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.INSTALLMENTS, "1234", LocalDate.now().plusYears(2), 20, 10);

        CardResponse res = service.create(1L, request);

        assertThat(res.id()).isEqualTo(500L);
        assertThat(res.displayName()).contains("Chase").contains("VISA").contains("PLATINUM").contains("1234");
    }

    @Test
    void create_rejectsDuplicateLast4() {
        when(bankRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(Bank.builder().id(10L).build()));
        when(cardRepository.existsByBankIdAndBrandAndCardTypeAndLast4Digits(any(), any(), any(), any())).thenReturn(true);

        CardRequest request = new CardRequest(10L, CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.INSTALLMENTS, "1234", LocalDate.now().plusYears(2), 20, 10);

        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void delete_removesCard() {
        Card card = Card.builder().id(500L).userId(1L).build();
        when(cardRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(card));

        service.delete(500L, 1L);

        verify(cardRepository).delete(card);
    }
}
