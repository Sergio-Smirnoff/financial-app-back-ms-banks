package com.financialapp.banks.service;

import com.financialapp.banks.exception.BusinessException;
import com.financialapp.banks.kafka.producer.BanksEventProducer;
import com.financialapp.banks.mapper.CardInstallmentMapper;
import com.financialapp.banks.model.dto.request.CardExpenseCreateRequest;
import com.financialapp.banks.model.dto.response.CardInstallmentResponse;
import com.financialapp.banks.model.entity.Card;
import com.financialapp.banks.model.entity.CardInstallment;
import com.financialapp.banks.model.enums.CardBehavior;
import com.financialapp.banks.repository.AccountRepository;
import com.financialapp.banks.repository.CardInstallmentRepository;
import com.financialapp.banks.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardInstallmentServiceTest {

    @Mock CardInstallmentRepository installmentRepository;
    @Mock CardRepository cardRepository;
    @Mock AccountService accountService;
    @Mock BanksEventProducer eventProducer;

    CardInstallmentMapper installmentMapper = new CardInstallmentMapper() {};

    CardInstallmentService service;

    @BeforeEach
    void setUp() {
        service = new CardInstallmentService(installmentRepository, cardRepository, accountService, installmentMapper, eventProducer);
    }

    @Test
    void createExpense_generatesMultipleInstallments() {
        Card card = Card.builder().id(500L).userId(1L).behavior(CardBehavior.INSTALLMENTS).build();
        when(cardRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(card));
        when(installmentRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CardExpenseCreateRequest request = new CardExpenseCreateRequest(
                "New Mac", BigDecimal.valueOf(3000), "USD", 3, LocalDate.of(2026, 5, 1));

        List<CardInstallmentResponse> res = service.createExpense(500L, 1L, request);

        assertThat(res).hasSize(3);
        assertThat(res.get(0).amount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(res.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(res.get(1).dueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(res.get(2).dueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    void createExpense_rejectsInstantPaymentCard() {
        Card card = Card.builder().id(500L).userId(1L).behavior(CardBehavior.INSTANT_PAYMENT).build();
        when(cardRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(card));

        CardExpenseCreateRequest request = new CardExpenseCreateRequest(
                "Coffee", BigDecimal.valueOf(10), "USD", 1, LocalDate.now());

        assertThatThrownBy(() -> service.createExpense(500L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not support installment-based expenses");
    }

    @Test
    void payInstallment_marksAsPaid() {
        Card card = Card.builder().id(500L).userId(1L).bankId(10L).build();
        CardInstallment installment = CardInstallment.builder().id(1000L).card(card).paid(false).amount(BigDecimal.TEN).currency("USD").build();
        
        when(cardRepository.findByIdAndUserId(500L, 1L)).thenReturn(Optional.of(card));
        when(installmentRepository.findById(1000L)).thenReturn(Optional.of(installment));
        when(installmentRepository.save(installment)).thenReturn(installment);

        CardInstallmentResponse res = service.payInstallment(500L, 1000L, 1L, 100L, LocalDate.now());

        assertThat(res.paid()).isTrue();
        assertThat(res.paidDate()).isNotNull();
    }
}
