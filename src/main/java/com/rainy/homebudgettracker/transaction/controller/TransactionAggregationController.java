package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;
import com.rainy.homebudgettracker.transaction.service.TransactionAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transaction-aggregation")
@RequiredArgsConstructor
public class TransactionAggregationController {
    private final TransactionAggregationService transactionAggregationService;

    @GetMapping("/sum-positive")
    public ResponseEntity<SumResponse> sumCurrentUserPositiveAmount(
            @RequestParam(name = "account-id") UUID accountId
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserPositiveAmount(accountId));
    }

    @GetMapping("/sum-negative")
    public ResponseEntity<SumResponse> sumCurrentUserNegativeAmount(
            @RequestParam(name = "account-id") UUID accountId
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserNegativeAmount(accountId));
    }

    @GetMapping("/sum")
    public ResponseEntity<SumResponse> sumCurrentUserAmount(
            @RequestParam(name = "account-id") UUID accountId
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserAmount(accountId));
    }

    @GetMapping("/sum-positive-by-date")
    public ResponseEntity<SumResponse> sumCurrentUserPositiveAmount(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserPositiveAmount(
                accountId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/sum-positive-by-date-and-category")
    public ResponseEntity<SumResponse> sumCurrentUserPositiveAmount(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "category-name") CategoryRequest category,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserPositiveAmount(
                accountId, category, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/sum-negative-by-date")
    public ResponseEntity<SumResponse> sumCurrentUserNegativeAmount(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserNegativeAmount(
                accountId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/sum-negative-by-date-and-category")
    public ResponseEntity<SumResponse> sumCurrentUserNegativeAmount(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam(name = "category-name") CategoryRequest category,
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserNegativeAmount(
                accountId, category, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/sum-totals-in-period")
    public ResponseEntity<List<SumResponse>> sumCurrentUserAmount(
            @RequestParam(name = "account-id") UUID accountId,
            @RequestParam() String date,
            @RequestParam(name = "type") PeriodType periodType
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserAmountInPeriod(
                accountId, LocalDate.parse(date), periodType));
    }

    @GetMapping("/sum-in-one-currency")
    public ResponseEntity<SumResponse> sumCurrentUserAmountInOneCurrency() {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserTotalAmountInDefaultCurrency());
    }

    @GetMapping("/top-five-incomes-converted")
    public ResponseEntity<List<SumResponse>> getCurrentUserTopFiveIncomesConvertedToDefaultCurrency(
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.getCurrentUserTopFiveIncomesConvertedToDefaultCurrency(
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/top-five-expenses-converted")
    public ResponseEntity<List<SumResponse>> getCurrentUserTopFiveExpensesConvertedToDefaultCurrency(
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.getCurrentUserTopFiveExpensesConvertedToDefaultCurrency(
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/sum-positive-in-default-currency")
    public ResponseEntity<SumResponse> sumCurrentUserPositiveAmountInDefaultCurrency(
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserPositiveAmountInDefaultCurrency(
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/sum-negative-in-default-currency")
    public ResponseEntity<SumResponse> sumCurrentUserNegativeAmountInDefaultCurrency(
            @RequestParam(name = "start-date") String startDate,
            @RequestParam(name = "end-date") String endDate
    ) {
        return ResponseEntity.ok(transactionAggregationService.sumCurrentUserNegativeAmountInDefaultCurrency(
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }
}
