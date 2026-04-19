package com.financialapp.banks.controller;

import com.financialapp.banks.model.dto.request.CardRequest;
import com.financialapp.banks.model.dto.response.ApiResponse;
import com.financialapp.banks.model.dto.response.CardResponse;
import com.financialapp.banks.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "User cards management")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "List user cards, optionally filtered by account")
    public ResponseEntity<ApiResponse<List<CardResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Long accountId) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.list(userId, accountId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single card")
    public ResponseEntity<ApiResponse<CardResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.get(id, userId)));
    }

    @PostMapping
    @Operation(summary = "Create a card")
    public ResponseEntity<ApiResponse<CardResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card created", cardService.create(userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a card")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        cardService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Card deleted", null));
    }
}
