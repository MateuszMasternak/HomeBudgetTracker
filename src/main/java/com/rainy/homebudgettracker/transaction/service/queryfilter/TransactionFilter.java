package com.rainy.homebudgettracker.transaction.service.queryfilter;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilter(
        UUID accountId,
        UUID categoryId,
        LocalDate startDate,
        LocalDate endDate
) {}
