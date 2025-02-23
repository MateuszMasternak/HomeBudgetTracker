package com.rainy.homebudgettracker.transaction;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
}
