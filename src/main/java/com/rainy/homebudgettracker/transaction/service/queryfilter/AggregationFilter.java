package com.rainy.homebudgettracker.transaction.service.queryfilter;

import com.rainy.homebudgettracker.transaction.enums.AmountType;

import java.time.LocalDate;
import java.util.UUID;

public record AggregationFilter(
        UUID accountId,
        UUID categoryId,
        LocalDate startDate,
        LocalDate endDate,
        AmountType amountType,
        boolean convertToDefaultCurrency,
        boolean historicalConversion
) {}
