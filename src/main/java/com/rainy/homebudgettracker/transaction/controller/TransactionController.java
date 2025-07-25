package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.TransactionUpdateRequest;
import com.rainy.homebudgettracker.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PatchMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransactionData(
            @PathVariable("id") UUID transactionId,
            @RequestBody @Valid TransactionUpdateRequest request) {
        return ResponseEntity.ok(transactionService.updateTransactionForCurrentUser(transactionId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentUserTransaction(@PathVariable("id") UUID transactionId) {
        transactionService.deleteCurrentUserTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<TransactionResponse> uploadImageForCurrentUserTransaction(
            @PathVariable("id") UUID transactionId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(transactionService.addImageToCurrentUserTransaction(transactionId, file));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<TransactionResponse> deleteImageForCurrentUserTransaction(@PathVariable("id") UUID transactionId) {
        return ResponseEntity.ok(transactionService.deleteImageFromCurrentUserTransaction(transactionId));
    }
}
