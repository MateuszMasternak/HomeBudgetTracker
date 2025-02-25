package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transaction/import")
@AllArgsConstructor
public class ImportTransactionController {
    private final ImportTransactionServiceImpl importTransactionService;

    @PostMapping("/csv/upload")
    public ResponseEntity<List<TransactionResponse>> uploadTransactionsFromPdf(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(importTransactionService.extractTransactions(file));
    }

    @PostMapping("/save")
    public ResponseEntity<Boolean> saveTransactions(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestBody List<TransactionRequest> transactions
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {
        importTransactionService.importTransactions(accountId, transactions);
        return ResponseEntity.accepted().build();
    }
}
