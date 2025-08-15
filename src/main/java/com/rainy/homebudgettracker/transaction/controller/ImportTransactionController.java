package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.enums.BankName;
import com.rainy.homebudgettracker.transaction.service.ImportTransactionServiceImpl;
import com.rainy.homebudgettracker.transaction.dto.TransactionRequest;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transaction/import")
@AllArgsConstructor
public class ImportTransactionController {
    private final ImportTransactionServiceImpl importTransactionService;

    @PostMapping("/csv/upload")
    public ResponseEntity<List<TransactionResponse>> uploadTransactionsFromCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bank-name")BankName bankName
            ) {
        System.out.println(importTransactionService.extractTransactions(file, bankName));
        return ResponseEntity.ok(importTransactionService.extractTransactions(file, bankName));
    }

    @PostMapping("/save")
    public ResponseEntity<Boolean> saveTransactions(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestBody List<TransactionRequest> transactions
    ) {
        importTransactionService.importTransactions(accountId, transactions);
        return ResponseEntity.accepted().build();
    }
}
