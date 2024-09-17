package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactionsByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        if (code == null) {
            return ResponseEntity.ok(transactionService.findAllByUser(user, pageable));
        } else {
            CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
            return ResponseEntity.ok(transactionService.findAllByUserAndCurrencyCode(user, currencyCode, pageable));
        }
    }

    @GetMapping("/category")
    public ResponseEntity<Iterable<TransactionResponse>> getAllTransactionsByUserAndCategory(
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code
    )
            throws RecordDoesNotExistException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        if (code == null) {
            return ResponseEntity.ok(transactionService.findAllByUserAndCategory(user, categoryName.toUpperCase(), pageable));
        } else {
            CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
            return ResponseEntity.ok(transactionService.findAllByUserAndCurrencyCodeAndCategory(
                    user,
                    currencyCode,
                    categoryName.toUpperCase(),
                    pageable
            ));
        }
    }

    @GetMapping("/date")
    public ResponseEntity<Iterable<TransactionResponse>> getAllTransactionsByUserAndDateBetween(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        if (code == null) {
            return ResponseEntity.ok(transactionService.findAllByUserAndDateBetween(
                    user,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate),
                    pageable
            ));
        } else {
            CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
            return ResponseEntity.ok(transactionService.findAllByUserAndCurrencyCodeAndDateBetween(
                    user,
                    currencyCode,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate),
                    pageable
            ));
        }
    }

    @GetMapping("/category-date")
    public ResponseEntity<Iterable<TransactionResponse>> getAllTransactionsByUserAndCategoryAndDateBetween(
            @RequestParam String categoryName,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code
    )
            throws RecordDoesNotExistException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        if (code == null) {
            return ResponseEntity.ok(transactionService.findAllByUserAndCategoryAndDateBetween(
                    user,
                    categoryName.toUpperCase(),
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate),
                    pageable
            ));
        } else {
            CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
            return ResponseEntity.ok(transactionService.findAllByUserAndCurrencyCodeAndCategoryAndDateBetween(
                    user,
                    currencyCode,
                    categoryName.toUpperCase(),
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate),
                    pageable
            ));
        }
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest transactionRequest)
            throws RecordDoesNotExistException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(transactionService.createTransaction(user, transactionRequest));
    }

    @PostMapping("/exchange")
    public ResponseEntity<TransactionResponse> createTransactionWithOtherCurrencyCode(
            @RequestParam String targetCurrency,
            @RequestParam(required = false) String exchangeRate,
            @RequestBody TransactionRequest transactionRequest
    )
            throws RecordDoesNotExistException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(targetCurrency.toUpperCase());
        return ResponseEntity.ok(transactionService.createTransaction(user, currencyCode, exchangeRate, transactionRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transactionService.deleteTransaction(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sum-positive")
    public ResponseEntity<SumResponse> sumPositiveAmountByUser(@RequestParam String code) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumPositiveAmountByUser(user, currencyCode));
    }

    @GetMapping("/sum-negative")
    public ResponseEntity<SumResponse> sumNegativeAmountByUser(@RequestParam String code) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumNegativeAmountByUser(user, currencyCode));
    }

    @GetMapping("/sum")
    public ResponseEntity<SumResponse> sumAmountByUser(@RequestParam String code) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumAmountByUser(user, currencyCode));
    }

    @GetMapping("/sum-date")
    public ResponseEntity<SumResponse> sumAmountByUserAndDateBetween(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String code
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumAmountByUserAndDateBetween(
                user,
                currencyCode,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactionsToCsv() throws IOException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        byte[] csvFileContent = transactionService.generateCsvFileForUserTransactions(user);

        HttpHeaders headers = new HttpHeaders();
        String fileName = "transactions_" + user.getId() + "_" + LocalDate.now() + ".csv";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(csvFileContent.length)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(csvFileContent);
    }
}
