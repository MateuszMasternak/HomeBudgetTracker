package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.dto.BalanceHistoryResponse;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;
import com.rainy.homebudgettracker.transaction.service.queryfilter.AggregationFilter;
import com.rainy.homebudgettracker.transaction.dto.SumResponse;
import com.rainy.homebudgettracker.transaction.enums.AmountType;
import com.rainy.homebudgettracker.transaction.service.TransactionAggregationService;
import com.rainy.homebudgettracker.transaction.service.queryfilter.PeriodicAggregationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/transactions")
@RequiredArgsConstructor
public class TransactionAggregationController {

    private final TransactionAggregationService aggregationService;

    @GetMapping("/summary")
    public ResponseEntity<SumResponse> getTransactionSummary(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "ALL") AmountType amountType,
            @RequestParam(defaultValue = "false") boolean convertToDefaultCurrency,
            @RequestParam(required = false, defaultValue = "false") boolean historicalConversion
    ) {
        AggregationFilter filter = new AggregationFilter(
                accountId, categoryId, startDate, endDate, amountType, convertToDefaultCurrency,
                historicalConversion
        );

        SumResponse sum = aggregationService.getSum(filter);

        return ResponseEntity.ok(sum);
    }

    @GetMapping("/top-categories")
    public ResponseEntity<List<SumResponse>> getTopFiveCategories(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam() AmountType categoryType,
            @RequestParam(defaultValue = "true") boolean convertToDefaultCurrency,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false, defaultValue = "false") boolean historicalConversion
    ) {
        AggregationFilter filter = new AggregationFilter(
                accountId, null, startDate, endDate, categoryType, convertToDefaultCurrency,
                historicalConversion
        );

        List<SumResponse> topFive = switch (categoryType) {
            case POSITIVE -> aggregationService.getTopFiveIncomes(filter);
            case NEGATIVE -> aggregationService.getTopFiveExpenses(filter);
            default -> throw new IllegalArgumentException("Unsupported category type: " + categoryType);
        };

        return ResponseEntity.ok(topFive);
    }

    @GetMapping("/balance-history")
    public ResponseEntity<BalanceHistoryResponse> getBalanceHistory(
            @RequestParam UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam PeriodType periodType
    ) {
        PeriodicAggregationFilter filter = new PeriodicAggregationFilter(accountId, date, periodType);
        BalanceHistoryResponse response = aggregationService.getBalanceHistory(filter);
        return ResponseEntity.ok(response);
    }
}
