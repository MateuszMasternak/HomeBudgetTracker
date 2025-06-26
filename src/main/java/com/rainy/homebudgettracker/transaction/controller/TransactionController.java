package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.handler.exception.*;
import com.rainy.homebudgettracker.transaction.*;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;
import com.rainy.homebudgettracker.transaction.enums.SortingParam;
import com.rainy.homebudgettracker.transaction.service.TransactionService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping()
    public ResponseEntity<Page<TransactionResponse>> getCurrentUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(defaultValue = "DATE", name = "sort-by") SortingParam sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {

        Pageable pageable = getPageRequest(page, size, sortBy, ascending);
        return ResponseEntity.ok(transactionService.findCurrentUserTransactionsAsResponses(accountId, pageable));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Page<TransactionResponse>> getCurrentUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "category-name") CategoryRequest category,
            @RequestParam(defaultValue = "DATE", name = "sort-by") SortingParam sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {

        Pageable pageable = getPageRequest(page, size, sortBy, ascending);
        return ResponseEntity.ok(
                transactionService.findCurrentUserTransactionsAsResponses(accountId, category, pageable));
    }

    @GetMapping("/by-date")
    public ResponseEntity<Page<TransactionResponse>> getCurrentUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate,
            @RequestParam(defaultValue = "DATE", name = "sort-by") SortingParam sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {

        Pageable pageable = getPageRequest(page, size, sortBy, ascending);
        return ResponseEntity.ok(
                transactionService.findCurrentUserTransactionsAsResponses(accountId, LocalDate.parse(startDate),
                        LocalDate.parse(endDate), pageable));
    }

    @GetMapping("/by-category-and-date")
    public ResponseEntity<Page<TransactionResponse>> getCurrentUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "category-name") CategoryRequest category,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate,
            @RequestParam(defaultValue = "DATE", name = "sort-by") SortingParam sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {

        Pageable pageable = getPageRequest(page, size, sortBy, ascending);
        return ResponseEntity.ok(
                transactionService.findCurrentUserTransactionsAsResponses(
                        accountId, category, LocalDate.parse(startDate), LocalDate.parse(endDate), pageable));
    }

    private Pageable getPageRequest(int page, int size, SortingParam sortingParam, boolean ascending) {
        Sort sort = switch (sortingParam) {
            case AMOUNT -> Sort.by("amount");
            case DATE -> Sort.by("date");
            case CATEGORY -> Sort.by("category");
        };

        if (!ascending) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        return PageRequest.of(page, size, sort);
    }

    @PostMapping("/create")
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @RequestBody @Valid TransactionRequest request,
            @RequestParam(name = "account-id") UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        return ResponseEntity.ok(transactionService.createTransactionForCurrentUser(accountId, request));
    }

    @PostMapping("/create-with-exchange-rate")
    public ResponseEntity<TransactionResponse> createTransactionForCurrentUser(
            @RequestBody @Valid TransactionRequest request,
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(required = false, name = "exchange-rate") String exchangeRate
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {

        BigDecimal rate = exchangeRate == null ? null : new BigDecimal(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);
        return ResponseEntity.ok(
                transactionService.createTransactionForCurrentUser(accountId, rate, request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCurrentUserTransaction(@RequestParam(name = "id") UUID transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        transactionService.deleteCurrentUserTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> getCurrentUserTransactionsAsCSV() throws IOException {

        byte[] content = transactionService.generateCSVWithCurrentUserTransactions();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "transactions.csv");
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<TransactionResponse> uploadImageForCurrentUserTransaction(
            @RequestParam(name = "id") UUID transactionId,
            @RequestParam("file") MultipartFile file
    ) throws RecordDoesNotExistException,
            UserIsNotOwnerException,
            ImageUploadException,
            WrongFileTypeException,
            PremiumStatusRequiredException {

        return ResponseEntity.accepted().body(
                transactionService.addImageToCurrentUserTransaction(transactionId, file));
    }

    @PostMapping("/delete-image")
    public ResponseEntity<TransactionResponse> deleteImageForCurrentUserTransaction(
            @RequestParam(name = "id") UUID transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        return ResponseEntity.accepted().body(transactionService.deleteImageFromCurrentUserTransaction(transactionId));
    }

    @PatchMapping("/update-data")
    public ResponseEntity<TransactionResponse> updateTransactionData(
            @RequestParam(name = "transaction-id") UUID transactionId,
            @RequestBody @Valid TransactionUpdateRequest request)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        return ResponseEntity.ok(transactionService.updateTransactionForCurrentUser(transactionId, request));
    }
}
