package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.card.command.IssueCardCommand;
import com.financialapp.banks.domain.usecase.card.command.CancelCardCommand;
import com.financialapp.banks.domain.usecase.card.command.UpdateCardCommand;
import com.financialapp.banks.domain.usecase.card.IssueCardUseCase;
import com.financialapp.banks.domain.usecase.card.CancelCardUseCase;
import com.financialapp.banks.domain.usecase.card.GetCardUseCase;
import com.financialapp.banks.domain.usecase.card.ListCardsUseCase;
import com.financialapp.banks.domain.usecase.card.UpdateCardUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.web.dto.request.CardRequest;
import com.financialapp.banks.web.dto.request.UpdateCardRequest;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
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
    private final IssueCardUseCase issueCardUseCase;
    private final CancelCardUseCase cancelCardUseCase;
    private final UpdateCardUseCase updateCardUseCase;
    private final CardWebMapper cardMapper;

    @GetMapping
    @Operation(summary = "List user cards, optionally filtered by bank")
    public ResponseEntity<ApiResponse<List<CardResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String bankNumber) {
        BankNumber bank = bankNumber != null ? new BankNumber(bankNumber) : null;
        List<CardResponse> result = listCardsUseCase.execute(new UserId(userId), bank)
                .stream().map(cardMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "Get a single card")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found"})
    public ResponseEntity<ApiResponse<CardResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable @Pattern(regexp = CARD_NUMBER_REGEX, message = CARD_NUMBER_MESSAGE) String cardNumber) {
        return ResponseEntity.ok(ApiResponse.ok(
                cardMapper.toResponse(getCardUseCase.execute(cardNumber, new UserId(userId)))));
    }

    @PostMapping
    @Operation(summary = "Create a card")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "resource_already_exists", "invalid_card_number", "invalid_issuer_bin", "invalid_issuer_card_account", "card_invalid_type"})
    public ResponseEntity<ApiResponse<CardResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CardRequest request) {
        var result = issueCardUseCase.execute(new IssueCardCommand(
                new UserId(userId),
                new BankNumber(request.bankNumber()),
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
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "card_expired", "card_invalid_type"})
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
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "card_not_deletable"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber) {
        cancelCardUseCase.execute(new CancelCardCommand(cardNumber, new UserId(userId)));
        return ResponseEntity.ok(ApiResponse.ok("Card deleted", null));
    }
}
