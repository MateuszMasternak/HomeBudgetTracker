package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "Get all transactions by user",
            description = "Get all transactions by user with pagination, optionally filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Get all transactions by user and category",
            description = "Get all transactions by user and category with pagination, optionally filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 402,
                                                        "businessErrorDescription": "Record does not exist or is not accessible"
                                                    }"""
                                    )
                            )
                    ),
            }
    )
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

    @Operation(
            summary = "Get all transactions by user and date between",
            description = "Get all transactions by user and date between with pagination, optionally filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 402,
                                                        "businessErrorDescription": "Record does not exist or is not accessible"
                                                    }"""
                                    )
                            )
                    ),
            }
    )
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

    @Operation(
            summary = "Get all transactions by user, category, and date between",
            description = "Get all transactions by user, category, and date between with pagination, optionally filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 402,
                                                        "businessErrorDescription": "Record does not exist or is not accessible"
                                                    }"""
                                    )
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Create a new transaction",
            description = "Create a new transaction",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = TransactionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 306,
                                                        "businessErrorDescription": "Missing or invalid request body element",
                                                        "validationErrors": [
                                                            "Date is required"
                                                        ]
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest transactionRequest)
            throws RecordDoesNotExistException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(transactionService.createTransaction(user, transactionRequest));
    }

    @Operation(
            summary = "Delete a transaction",
            description = "Delete a transaction",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No Content"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 402,
                                                        "businessErrorDescription": "Record does not exist or is not accessible"
                                                    }"""
                                    )
                            )
                    ),
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transactionService.deleteTransaction(user, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Sum positive amounts",
            description = "Sum positive amounts filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SumResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 400,
                                                        "businessErrorDescription": "Missing request parameter: code"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
    @GetMapping("/sum-positive")
    public ResponseEntity<SumResponse> sumPositiveAmountByUser(@RequestParam String code) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumPositiveAmountByUser(user, currencyCode));
    }

    @Operation(
            summary = "Sum negative amounts",
            description = "Sum negative amounts filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SumResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 400,
                                                        "businessErrorDescription": "Missing request parameter: code"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
    @GetMapping("/sum-negative")
    public ResponseEntity<SumResponse> sumNegativeAmountByUser(@RequestParam String code) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumNegativeAmountByUser(user, currencyCode));
    }

    @Operation(
            summary = "Sum amounts",
            description = "Sum amounts filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SumResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 400,
                                                        "businessErrorDescription": "Missing request parameter: code"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
    @GetMapping("/sum")
    public ResponseEntity<SumResponse> sumAmountByUser(@RequestParam String code) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(transactionService.sumAmountByUser(user, currencyCode));
    }

    @Operation(
            summary = "Sum amounts by date between",
            description = "Sum amounts by date between filtered by currency code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SumResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 400,
                                                        "businessErrorDescription": "Missing request parameter: startDate"
                                                    }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Export transactions to CSV",
            description = "Export transactions to CSV",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/csv"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    )
                            )
                    )
            }
    )
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
