package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.handler.exception.ImageUploadException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.handler.exception.WrongFileTypeException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping()
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam CurrencyCode code
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccount(code, pageable));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccountAndCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam CurrencyCode code,
            @RequestParam CategoryRequest category
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccountAndCategory(code, category, pageable));
    }

    @GetMapping("/by-date")
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccountAndDateBetween(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam CurrencyCode code,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                code, LocalDate.parse(startDate), LocalDate.parse(endDate), pageable));
    }

    @GetMapping("/by-category-and-date")
    public ResponseEntity<Page<TransactionResponse>> getAllByCurrentUserAndAccountAndCategoryAndDateBetween(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String code,
            @RequestParam String category,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) throws RecordDoesNotExistException {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
                currencyCode, category, LocalDate.parse(startDate), LocalDate.parse(endDate), pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @RequestBody @Valid TransactionRequest request) throws RecordDoesNotExistException {
        return ResponseEntity.ok(transactionService.createTransactionForCurrentUser(request));
    }

    @PostMapping("/create-with-exchange-rate")
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @RequestBody @Valid TransactionRequest request,
            @RequestParam(name = "target-currency") String targetCurrency,
            @RequestParam(required = false, name = "exchange-rate") String exchangeRate
    ) throws RecordDoesNotExistException {
        CurrencyCode currencyCode = CurrencyCode.valueOf(targetCurrency.toUpperCase());
        BigDecimal rate = exchangeRate == null ? null : new BigDecimal(exchangeRate);
        return ResponseEntity.ok(transactionService.createTransactionForCurrentUser(currencyCode, rate, request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCurrentUserTransaction(@RequestParam Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        transactionService.deleteCurrentUserTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sum-positive")
    public ResponseEntity<SumResponse> sumPositiveAmountByCurrentUserAndAccount(
            @RequestParam CurrencyCode code
    ) throws RecordDoesNotExistException {
        return ResponseEntity.ok(transactionService.sumPositiveAmountByCurrentUserAndAccount(code));
    }

    @GetMapping("/sum-negative")
    public ResponseEntity<SumResponse> sumNegativeAmountByCurrentUserAndAccount(
            @RequestParam CurrencyCode code
    ) throws RecordDoesNotExistException {
        return ResponseEntity.ok(transactionService.sumNegativeAmountByCurrentUserAndAccount(code));
    }

    @GetMapping("/sum")
    public ResponseEntity<SumResponse> sumAmountByCurrentUserAndAccount(
            @RequestParam CurrencyCode code
    ) throws RecordDoesNotExistException {
        return ResponseEntity.ok(transactionService.sumAmountByCurrentUserAndAccount(code));
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportTransactionsToCsv() throws IOException {
        byte[] content = transactionService.generateCsvFileForCurrentUserTransactions();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "transactions.csv");
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<TransactionResponse> uploadImageForTransaction(
            @RequestParam Long id,
            @RequestParam("file") MultipartFile file
    ) throws RecordDoesNotExistException, UserIsNotOwnerException, ImageUploadException, WrongFileTypeException {
        return ResponseEntity.accepted().body(transactionService.addImageToTransaction(id, file));
    }

    @PostMapping("/delete-image")
    public ResponseEntity<TransactionResponse> deleteImageForTransaction(@RequestParam Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        return ResponseEntity.accepted().body(transactionService.deleteImageFromTransaction(id));
    }
}
