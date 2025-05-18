package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {

    @GetMapping()
    public ResponseEntity<List<CurrencyCode>> getAllCurrencies() {
        return ResponseEntity.ok(Arrays.stream(CurrencyCode.values()).toList());
    }
}
