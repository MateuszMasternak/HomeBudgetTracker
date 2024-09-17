package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String code
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccount(currencyCode, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccountAndCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String code,
            @RequestParam String category
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccountAndCategory(currencyCode, category, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccountAndDateBetween(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String code,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                currencyCode, LocalDate.parse(startDate), LocalDate.parse(endDate), pageable));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccountAndCategoryAndDateBetween(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String code,
            @RequestParam String category,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
                currencyCode, category, LocalDate.parse(startDate), LocalDate.parse(endDate), pageable));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @RequestBody TransactionRequest request) throws RecordDoesNotExistException {
        return ResponseEntity.ok(transactionService.createTransactionForCurrentUser(request));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @RequestBody TransactionRequest request,
            @RequestParam String targetCurrency,
            @RequestParam(required = false) String exchangeRate
    ) throws RecordDoesNotExistException {
        CurrencyCode currencyCode = CurrencyCode.valueOf(targetCurrency.toUpperCase());
        BigDecimal rate = exchangeRate == null ? null : new BigDecimal(exchangeRate);
        return ResponseEntity.ok(transactionService.createTransactionForCurrentUser(currencyCode, rate, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentUserTransaction(@RequestParam Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        transactionService.deleteCurrentUserTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<SumResponse> sumPositiveAmountByCurrentUserAndAccount(
            @RequestParam String code
    ) throws RecordDoesNotExistException {
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumPositiveAmountByCurrentUserAndAccount(currencyCode));
    }

    @GetMapping
    public ResponseEntity<SumResponse> sumNegativeAmountByCurrentUserAndAccount(
            @RequestParam String code
    ) throws RecordDoesNotExistException {
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumNegativeAmountByCurrentUserAndAccount(currencyCode));
    }

    @GetMapping
    public ResponseEntity<SumResponse> sumAmountByCurrentUserAndAccount(
            @RequestParam String code
    ) throws RecordDoesNotExistException {
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumAmountByCurrentUserAndAccount(currencyCode));
    }

    @GetMapping
    public ResponseEntity<byte[]> exportTransactionsToCsv() throws IOException {
        byte[] content = transactionService.generateCsvFileForCurrentUserTransactions();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "transactions.csv");
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
