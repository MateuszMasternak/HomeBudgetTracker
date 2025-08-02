package com.rainy.homebudgettracker.transaction.service.queryfilter;

import com.rainy.homebudgettracker.transaction.enums.PeriodType;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodicAggregationFilter(
        UUID accountId,
        LocalDate date,
        PeriodType periodType
) {}
