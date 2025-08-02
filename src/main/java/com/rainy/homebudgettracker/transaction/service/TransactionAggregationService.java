package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.transaction.BalanceHistoryResponse;
import com.rainy.homebudgettracker.transaction.service.queryfilter.AggregationFilter;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.transaction.service.queryfilter.PeriodicAggregationFilter;

import java.util.List;

public interface TransactionAggregationService {
    SumResponse getSum(AggregationFilter filter);

    List<SumResponse> getTopFiveIncomes(AggregationFilter filter);

    List<SumResponse> getTopFiveExpenses(AggregationFilter filter);

    BalanceHistoryResponse getBalanceHistory(PeriodicAggregationFilter filter);
}
