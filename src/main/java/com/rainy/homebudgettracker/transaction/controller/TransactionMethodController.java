package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction-method")
@RequiredArgsConstructor
public class TransactionMethodController {
    @GetMapping()
    public ResponseEntity<List<TransactionMethod>> getAllTransactionMethods() {
        return ResponseEntity.ok(Arrays.asList(TransactionMethod.values()));
    }
}
