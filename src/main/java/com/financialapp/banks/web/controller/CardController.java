package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.card.command.CreateCardCommand;
import com.financialapp.banks.domain.usecase.card.command.DeleteCardCommand;
import com.financialapp.banks.domain.usecase.card.command.UpdateCardCommand;
import com.financialapp.banks.domain.usecase.card.CreateCardUseCase;
import com.financialapp.banks.domain.usecase.card.DeleteCardUseCase;
import com.financialapp.banks.domain.usecase.card.GetCardUseCase;
import com.financialapp.banks.domain.usecase.card.ListCardsUseCase;
import com.financialapp.banks.domain.usecase.card.UpdateCardUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.web.dto.request.CardRequest;
import com.financialapp.banks.web.dto.request.UpdateCardRequest;
import com.financialapp.banks.web.dto.response.ApiResponse;
import com.financialapp.banks.web.dto.response.CardResponse;
import com.financialapp.banks.web.mapper.CardWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/cards")
@RequiredArgsConstructor
@Validated
@Tag(name = "Cards", description = "User cards management")
public class CardController {

    private static final String CARD_NUMBER_REGEX = "^\\d{16}$";
    private static final String CARD_NUMBER_MESSAGE = "Card number must be exactly 16 digits";

    private final ListCardsUseCase listCardsUseCase;
    private final GetCardUseCase getCardUseCase;
    private final CreateCardUseCase createCardUseCase;
    private final DeleteCardUseCase deleteCardUseCase;
    private final UpdateCardUseCase updateCardUseCase;
    private final CardWebMapper cardMapper;

    @GetMapping
    @Operation(summary = "List user cards, optionally filtered by bank")
    public ResponseEntity<ApiResponse<List<CardResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String bankName) {
        BankName bank = bankName != null ? BankName.fromString(bankName) : null;
        List<CardResponse> result = listCardsUseCase.execute(new UserId(userId), bank)
                .stream().map(cardMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "Get a single card")
    public ResponseEntity<ApiResponse<CardResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable @Pattern(regexp = CARD_NUMBER_REGEX, message = CARD_NUMBER_MESSAGE) String cardNumber) {
        return ResponseEntity.ok(ApiResponse.ok(
                cardMapper.toResponse(getCardUseCase.execute(cardNumber, new UserId(userId)))));
    }

    @PostMapping
    @Operation(summary = "Create a card")
    public ResponseEntity<ApiResponse<CardResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CardRequest request) {
        var result = createCardUseCase.execute(new CreateCardCommand(
                new UserId(userId),
                BankName.fromString(request.bankName()),
                request.brand(),
                request.cardType(),
                request.behavior(),
                request.cardNumber(),
                request.expiringDate(),
                request.closingDay(),
                request.dueDay()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Card created", cardMapper.toResponse(result)));
    }

    @PatchMapping("/{cardNumber}")
    @Operation(summary = "Update card billing and expiry date")
    public ResponseEntity<ApiResponse<CardResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber,
            @Valid @RequestBody UpdateCardRequest request) {
        Card result = updateCardUseCase.execute(new UpdateCardCommand(
                cardNumber,
                new UserId(userId),
                request.expiringDate(),
                request.closingDay(),
                request.dueDay()
        ));
        return ResponseEntity.ok(ApiResponse.ok("Card updated", cardMapper.toResponse(result)));
    }

    @DeleteMapping("/{cardNumber}")
    @Operation(summary = "Delete a card")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber) {
        deleteCardUseCase.execute(new DeleteCardCommand(cardNumber, new UserId(userId)));
        return ResponseEntity.ok(ApiResponse.ok("Card deleted", null));
    }
}
