package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.transaction.dto.BalanceHistoryResponse;
import com.rainy.homebudgettracker.transaction.service.queryfilter.AggregationFilter;
import com.rainy.homebudgettracker.transaction.dto.SumResponse;
import com.rainy.homebudgettracker.transaction.service.queryfilter.PeriodicAggregationFilter;

import java.util.List;

public interface TransactionAggregationService {
    SumResponse getSum(AggregationFilter filter);

    List<SumResponse> getTopFiveIncomes(AggregationFilter filter);

    List<SumResponse> getTopFiveExpenses(AggregationFilter filter);

    BalanceHistoryResponse getBalanceHistory(PeriodicAggregationFilter filter);
}
