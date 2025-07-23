package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.*;
import com.rainy.homebudgettracker.transaction.service.TransactionService;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class AccountTransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> findCurrentUserTransactions(
            @PathVariable UUID accountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        TransactionFilter filter = new TransactionFilter(accountId, categoryId, startDate, endDate);
        Page<TransactionResponse> transactions = transactionService.findCurrentUserTransactions(filter, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @PathVariable UUID accountId,
            @RequestBody @Valid TransactionRequest request) {
        TransactionResponse response = transactionService.createTransactionForCurrentUser(accountId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/accounts/{accountId}/transactions/{transactionId}")
                .buildAndExpand(accountId, response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
