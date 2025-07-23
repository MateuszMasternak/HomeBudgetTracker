package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions/export")
@RequiredArgsConstructor
public class ExportTransactionController {
    private final TransactionService transactionService;

    @GetMapping()
    public ResponseEntity<byte[]> getCurrentUserTransactionsAsCSV() {
        byte[] content = transactionService.generateCSVWithCurrentUserTransactions();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "transactions.csv");
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
