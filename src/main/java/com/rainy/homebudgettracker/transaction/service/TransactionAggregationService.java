package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionAggregationService {
    SumResponse sumCurrentUserPositiveAmount(UUID accountId);

    SumResponse sumCurrentUserNegativeAmount(UUID accountId);

    SumResponse sumCurrentUserAmount(UUID accountId);

    SumResponse sumCurrentUserPositiveAmount(UUID accountId, LocalDate startDate, LocalDate endDate);

    SumResponse sumCurrentUserPositiveAmount(
            UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate);

    SumResponse sumCurrentUserNegativeAmount(UUID accountId, LocalDate startDate, LocalDate endDate);

    SumResponse sumCurrentUserNegativeAmount(
            UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate);

    List<SumResponse> sumCurrentUserAmountInPeriod(UUID accountId, LocalDate date, PeriodType periodType);

    SumResponse sumCurrentUserTotalAmountInDefaultCurrency();

    List<SumResponse> getCurrentUserTopFiveIncomesConvertedToDefaultCurrency(
            LocalDate startDate, LocalDate endDate);

    List<SumResponse> getCurrentUserTopFiveExpensesConvertedToDefaultCurrency(
            LocalDate startDate, LocalDate endDate);

    SumResponse sumCurrentUserPositiveAmountInDefaultCurrency(LocalDate startDate, LocalDate endDate);

    SumResponse sumCurrentUserNegativeAmountInDefaultCurrency(LocalDate startDate, LocalDate endDate);
}
